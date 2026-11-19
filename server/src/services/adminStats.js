const User = require('../models/User');
const Project = require('../models/Project');
const Task = require('../models/Task');
const JoinRequest = require('../models/JoinRequest');
const Invitation = require('../models/Invitation');
const Report = require('../models/Report');
const PlatformSetting = require('../models/PlatformSetting');

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const GROWTH_MONTHS = 7;
const TOP_SKILLS = 5;

const round1 = (n) => Math.round(n * 10) / 10;
const pct = (part, whole) => (whole > 0 ? round1((part / whole) * 100) : 0);

/**
 * Members / capacity of a project team. Projects created by students carry a real
 * members array; projects created from the admin panel only carry a "3 / 6" string.
 */
function teamCounts(p) {
  const capacity = p.maxMembers || 6;
  const memberCount = (p.members && p.members.length) || 0;
  if (memberCount > 0) return { members: memberCount, capacity };
  const m = /(\d+)\s*\/\s*(\d+)/.exec(p.teamSize || '');
  if (m) return { members: Number(m[1]), capacity: Number(m[2]) || capacity };
  return { members: 1, capacity };
}

/** "3 / 6" label shown in the admin projects table, consistent with teamCounts(). */
function teamSizeLabel(p) {
  const { members, capacity } = teamCounts(p);
  return `${members} / ${capacity}`;
}

/** Growth of a total over the current month, relative to where it stood at the start of the month. */
function monthOverMonth(total, addedThisMonth) {
  const before = total - addedThisMonth;
  if (before <= 0) return addedThisMonth > 0 ? 100 : 0;
  return pct(addedThisMonth, before);
}

/** Map of lower-cased skill name -> { name, count } built from every user profile. */
async function profileSkillCounts() {
  const rows = await User.aggregate([
    { $unwind: '$skills' },
    { $group: { _id: { $toLower: { $trim: { input: '$skills.name' } } }, name: { $first: '$skills.name' }, users: { $addToSet: '$_id' } } },
    { $project: { name: 1, count: { $size: '$users' } } },
  ]);
  const map = new Map();
  for (const r of rows) {
    if (r._id) map.set(r._id, { name: String(r.name).trim(), count: r.count });
  }
  return map;
}

/** Map of lower-cased category name -> number of projects in it. */
async function projectCategoryCounts() {
  const rows = await Project.aggregate([
    { $group: { _id: { $toLower: { $trim: { input: { $ifNull: ['$category', ''] } } } }, count: { $sum: 1 } } },
  ]);
  const map = new Map();
  for (const r of rows) if (r._id) map.set(r._id, r.count);
  return map;
}

/**
 * Every number on the admin Overview, computed from MongoDB.
 * Definitions (mirrored by AdminDataStore on the Java side):
 *  - successRate: Completed / (Active + Under review + Completed) — of the projects that got
 *    past recruiting, how many were delivered.
 *  - active team: a project that is not Completed, not archived, and has 2+ members.
 *  - taskCompletion: done tasks / all tasks of non-archived projects.
 *  - engagementRate: share of users who are a member of at least one project.
 */
async function computeOverviewStats({ skillCounts } = {}) {
  const now = new Date();
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
  const windowStart = new Date(now.getFullYear(), now.getMonth() - (GROWTH_MONTHS - 1), 1);

  const [
    settings,
    totalUsers,
    suspendedUsers,
    usersBeforeWindow,
    recentUsers,
    projects,
    joinAgg,
    inviteAgg,
    pendingReportsCount,
    usersInTeams,
    skills,
  ] = await Promise.all([
    PlatformSetting.findOne({ key: 'global' }).lean(),
    User.countDocuments({}),
    User.countDocuments({ status: 'Suspended' }),
    User.countDocuments({ createdAt: { $lt: windowStart } }),
    User.find({ createdAt: { $gte: windowStart } }).select('createdAt').lean(),
    Project.find({}).select('status archived members maxMembers teamSize milestones createdAt').lean(),
    JoinRequest.aggregate([{ $group: { _id: '$status', count: { $sum: 1 } } }]),
    Invitation.aggregate([{ $group: { _id: '$status', count: { $sum: 1 } } }]),
    Report.countDocuments({ status: { $in: ['Pending', 'In review'] } }),
    Project.distinct('members.user'),
    skillCounts ? Promise.resolve(skillCounts) : profileSkillCounts(),
  ]);
  // Project member lists can still reference deleted accounts — only count users that exist.
  const engagedUsers = usersInTeams.length ? await User.countDocuments({ _id: { $in: usersInTeams } }) : 0;

  const maxTeamSize = (settings && settings.maxTeamSize) || 6;

  // ---- Users & monthly growth ----
  const buckets = [];
  for (let i = GROWTH_MONTHS - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    buckets.push({ key: d.getFullYear() * 12 + d.getMonth(), label: MONTHS[d.getMonth()], year: d.getFullYear(), newUsers: 0, total: 0 });
  }
  const byKey = new Map(buckets.map((b) => [b.key, b]));
  for (const u of recentUsers) {
    const d = new Date(u.createdAt);
    const b = byKey.get(d.getFullYear() * 12 + d.getMonth());
    if (b) b.newUsers += 1;
  }
  let running = usersBeforeWindow;
  for (const b of buckets) {
    running += b.newUsers;
    b.total = running;
  }
  // Users created without a timestamp (very old data) are folded into the last point.
  buckets[buckets.length - 1].total = totalUsers;
  const newUsersThisMonth = buckets[buckets.length - 1].newUsers;
  const newUsersLastMonth = buckets.length > 1 ? buckets[buckets.length - 2].newUsers : 0;

  // ---- Projects lifecycle ----
  const status = { active: 0, recruiting: 0, underReview: 0, completed: 0 };
  let newProjectsThisMonth = 0;
  let milestonesTotal = 0;
  let milestonesCompleted = 0;
  let activeTeams = 0;
  let teamMembers = 0;
  let teamCapacity = 0;
  const capacity = { full: 0, openSeats: 0, overLimit: 0 };

  for (const p of projects) {
    if (p.status === 'Active') status.active++;
    else if (p.status === 'Recruiting') status.recruiting++;
    else if (p.status === 'Under review') status.underReview++;
    else if (p.status === 'Completed') status.completed++;

    if (p.createdAt && new Date(p.createdAt) >= monthStart) newProjectsThisMonth++;

    for (const m of p.milestones || []) {
      milestonesTotal++;
      if (m.status === 'Completed') milestonesCompleted++;
    }

    const t = teamCounts(p);
    if (!p.archived && p.status !== 'Completed' && t.members >= 2) {
      activeTeams++;
      teamMembers += t.members;
      teamCapacity += t.capacity;
      if (t.members > maxTeamSize) capacity.overLimit++;
      else if (t.members >= t.capacity) capacity.full++;
      else capacity.openSeats++;
    }
  }
  const totalProjects = projects.length;
  const startedProjects = status.active + status.underReview + status.completed;

  // ---- Collaboration requests (join requests + teammate invitations) ----
  const reqCounts = { pending: 0, accepted: 0, declined: 0 };
  for (const row of [...joinAgg, ...inviteAgg]) {
    if (row._id in reqCounts) reqCounts[row._id] += row.count;
  }
  const collaborationRequests = reqCounts.pending + reqCounts.accepted + reqCounts.declined;
  const decidedRequests = reqCounts.accepted + reqCounts.declined;

  // ---- Tasks (only projects still on the board) ----
  const liveProjectIds = projects.filter((p) => !p.archived).map((p) => p._id);
  const taskAgg = await Task.aggregate([
    { $match: { project: { $in: liveProjectIds } } },
    { $group: { _id: null, total: { $sum: 1 }, done: { $sum: { $cond: [{ $eq: ['$status', 'done'] }, 1, 0] } } } },
  ]);
  const tasksTotal = taskAgg.length ? taskAgg[0].total : 0;
  const tasksDone = taskAgg.length ? taskAgg[0].done : 0;

  const topSkills = [...skills.values()]
    .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name))
    .slice(0, TOP_SKILLS);

  return {
    generatedAt: now.toISOString(),

    totalUsers,
    activeUsers: totalUsers - suspendedUsers,
    suspendedUsers,
    newUsersThisMonth,
    newUsersLastMonth,
    userGrowthPct: monthOverMonth(totalUsers, newUsersThisMonth),
    engagementRate: pct(engagedUsers, totalUsers),
    monthlyGrowth: buckets.map(({ label, year, newUsers, total }) => ({ label, year, newUsers, total })),

    totalProjects,
    newProjectsThisMonth,
    projectGrowthPct: monthOverMonth(totalProjects, newProjectsThisMonth),
    activeProjectsCount: status.active,
    recruitingProjectsCount: status.recruiting,
    underReviewProjectsCount: status.underReview,
    completedProjectsCount: status.completed,
    projectSuccessRate: pct(status.completed, startedProjects),
    milestonesTotal,
    milestonesCompleted,

    activeTeamsCount: activeTeams,
    averageTeamSize: activeTeams ? round1(teamMembers / activeTeams) : 0,
    averageTeamCapacity: activeTeams ? round1(teamCapacity / activeTeams) : 0,
    teamFillRatio: pct(teamMembers, teamCapacity),
    teamCapacity: capacity,
    maxTeamSize,

    collaborationRequests,
    pendingCollaborationRequests: reqCounts.pending,
    requestAcceptanceRate: pct(reqCounts.accepted, decidedRequests),

    tasksTotal,
    tasksDone,
    sprintVelocity: Math.round(pct(tasksDone, tasksTotal)),

    topSkills,
    pendingReportsCount,
  };
}

module.exports = { computeOverviewStats, profileSkillCounts, projectCategoryCounts, teamSizeLabel };
