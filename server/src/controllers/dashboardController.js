const User = require('../models/User');
const Project = require('../models/Project');
const Task = require('../models/Task');
const JoinRequest = require('../models/JoinRequest');
const Invitation = require('../models/Invitation');
const Activity = require('../models/Activity');
const Notification = require('../models/Notification');
const Review = require('../models/Review');
const f = require('../utils/format');

const USER_CARD_FIELDS =
  'name headline role university avatarBg avatarFg availability experience skills portfolio github bio ' +
  'matchHint matchDesc ratingBase reviewsBase settings.visibility';

const ACTIVITY_BADGES = {
  design: ['Design', '#EDE9FE', '#6D28D9'],
  completed: ['Completed', '#DCFCE7', '#16A34A'],
  task: ['Task', '#F1F5F9', '#475569'],
  joined: ['Joined', '#E0F2FE', '#0369A1'],
  created: ['New', '#FDEEF4', '#EA4C89'],
  file: ['File', '#FEF3C7', '#B45309'],
  review: ['Review', '#FCE7F3', '#BE185D'],
};

const idStr = (v) => String((v && v._id) || v);

// ---------------------------------------------------------------------------
// Scoring helpers
// ---------------------------------------------------------------------------

/** Skill-match between the current user and another student (0-99). */
function matchFor(me, other) {
  const mine = new Set((me.skills || []).map((s) => s.name.toLowerCase()));
  const theirs = (other.skills || []).map((s) => s.name);
  const hint = other.matchHint || 0;

  if (mine.size === 0) {
    const pct = hint || 60 + (theirs.length * 7) % 25;
    const desc = other.matchDesc ||
      (theirs.length ? `Brings ${theirs.slice(0, 2).join(' & ')} to your next team.` : 'New on TeamUp — say hello!');
    return { pct, desc };
  }

  const overlap = theirs.filter((s) => mine.has(s.toLowerCase()));
  const complement = theirs.filter((s) => !mine.has(s.toLowerCase()));
  let computed = 55 + overlap.length * 9 + Math.min(complement.length, 3) * 5;
  computed = Math.min(97, computed);
  const pct = hint ? Math.round((hint + computed) / 2) : computed;

  let desc;
  if (overlap.length && complement.length) {
    desc = `Shares ${overlap.slice(0, 2).join(' & ')} with you and adds ${complement[0]}.`;
  } else if (overlap.length) {
    desc = `Shares your ${overlap.slice(0, 2).join(' & ')} skills.`;
  } else if (complement.length) {
    desc = `Complements you with ${complement.slice(0, 2).join(' & ')}.`;
  } else {
    desc = other.matchDesc || 'Open to new collaborations.';
  }
  return { pct, desc };
}

function ratingFor(user, agg) {
  const a = agg.get(idStr(user)) || { sum: 0, count: 0 };
  const baseCount = user.reviewsBase || 0;
  const count = baseCount + a.count;
  if (count === 0) return { rating: 0, reviews: 0 };
  const rating = ((user.ratingBase || 0) * baseCount + a.sum) / count;
  return { rating: Math.round(rating * 10) / 10, reviews: count };
}

function profileCompleteness(me) {
  const checks = [
    !!me.headline,
    !!me.university,
    !!me.bio,
    !!(me.portfolio || me.github),
    (me.skills || []).length >= 3,
  ];
  return 40 + checks.filter(Boolean).length * 12;
}

function projectProgress(project, tasks) {
  if (project.status === 'Completed') return 100;
  if (!tasks.length) return 0;
  return Math.round((tasks.filter((t) => t.status === 'done').length * 100) / tasks.length);
}

function openRolesLabel(p) {
  const roles = p.openRoles || [];
  if (p.status !== 'Recruiting' || roles.length === 0) return 'Team is not recruiting';
  return `Open: ${roles.slice(0, 2).join(' · ')}`;
}

// ---------------------------------------------------------------------------
// GET /api/dashboard — everything the student dashboard renders, in one call
// ---------------------------------------------------------------------------

const getDashboard = async (req, res) => {
  try {
    const now = new Date();
    const me = await User.findById(req.user._id).select('-password');
    const meId = idStr(me);

    const [allProjects, others, reviewAgg] = await Promise.all([
      Project.find({ archived: false })
        .populate('owner', 'name avatarBg avatarFg')
        .sort({ createdAt: -1 }),
      User.find({ _id: { $ne: me._id }, role: { $ne: 'Admin' }, 'settings.visibility': { $ne: 'PRIVATE' } })
        .select(USER_CARD_FIELDS)
        .sort({ createdAt: 1 }),
      Review.aggregate([{ $group: { _id: '$reviewee', sum: { $sum: '$rating' }, count: { $sum: 1 } } }]),
    ]);
    const ratingAgg = new Map(reviewAgg.map((r) => [String(r._id), r]));

    const myProjects = await Project.find({ 'members.user': me._id })
      .populate('owner', 'name')
      .populate('members.user', 'name avatarBg avatarFg headline')
      .sort({ archived: 1, createdAt: -1 });
    const myProjectIds = myProjects.map((p) => p._id);
    const leadProjectIds = myProjects.filter((p) => idStr(p.owner) === meId).map((p) => p._id);

    const [projectTasks, candidates, sentJoin, sentInvites, receivedInvites, activities, notifications, myReviews] =
      await Promise.all([
        Task.find({ project: { $in: myProjectIds } }).populate('project', 'title').sort({ dueDate: 1, createdAt: 1 }),
        JoinRequest.find({ project: { $in: leadProjectIds }, status: 'pending' })
          .populate('user', 'name avatarBg avatarFg skills headline')
          .sort({ createdAt: -1 }),
        JoinRequest.find({ user: me._id, status: 'pending' }).select('project'),
        Invitation.find({ from: me._id, status: 'pending' }).select('to kind'),
        Invitation.countDocuments({ to: me._id, status: 'pending' }),
        Activity.find({ project: { $in: myProjectIds }, actor: { $ne: me._id } })
          .populate('actor', 'name avatarBg avatarFg')
          .populate('project', 'title')
          .sort({ createdAt: -1 })
          .limit(12),
        Notification.find({ user: me._id }).sort({ createdAt: -1 }).limit(8),
        Review.find({ reviewee: me._id })
          .populate('reviewer', 'name headline avatarBg avatarFg')
          .populate('project', 'title')
          .sort({ createdAt: -1 }),
      ]);

    const tasksByProject = new Map();
    for (const t of projectTasks) {
      const k = idStr(t.project);
      if (!tasksByProject.has(k)) tasksByProject.set(k, []);
      tasksByProject.get(k).push(t);
    }
    const candidatesByProject = new Map();
    for (const c of candidates) {
      const k = idStr(c.project);
      if (!candidatesByProject.has(k)) candidatesByProject.set(k, []);
      candidatesByProject.get(k).push(c);
    }

    const bookmarked = new Set((me.bookmarks || []).map(String));
    const requestedProjects = new Set(sentJoin.map((r) => idStr(r.project)));
    const invitedUsers = new Set(sentInvites.filter((i) => i.kind === 'invite').map((i) => idStr(i.to)));
    const collabUsers = new Set(sentInvites.filter((i) => i.kind === 'collab').map((i) => idStr(i.to)));
    const mySkillSet = new Set((me.skills || []).map((s) => s.name.toLowerCase()));

    // ---- Discover projects ----
    const discover = allProjects.map((p) => {
      const owner = p.owner || { name: 'TeamUp' };
      return {
        id: idStr(p),
        title: p.title,
        category: p.category,
        experience: p.experience,
        description: p.description,
        skills: p.skills,
        recruiting: p.status === 'Recruiting',
        status: p.status,
        currentMembers: p.members.length,
        maxMembers: p.maxMembers,
        openRoles: openRolesLabel(p),
        openRoleCount: p.status === 'Recruiting' ? (p.openRoles || []).length : 0,
        creatorName: owner.name,
        creatorInitials: f.initials(owner.name),
        creatorColor: owner.avatarFg || '#EA4C89',
        iconType: p.iconType,
        iconBg: p.iconBg,
        iconColor: p.iconColor,
        bookmarked: bookmarked.has(idStr(p)),
        isMember: p.isMember(me._id),
        requested: requestedProjects.has(idStr(p)),
      };
    });

    // ---- Teammates ----
    const teammates = others
      .map((u) => {
        const m = matchFor(me, u);
        const r = ratingFor(u, ratingAgg);
        return {
          id: idStr(u),
          name: u.name,
          role: u.headline || u.role || 'Student',
          university: u.university || 'TeamUp member',
          initials: f.initials(u.name),
          avatarBg: u.avatarBg,
          avatarFg: u.avatarFg,
          availability: u.availability || 'Availability not set',
          rating: r.rating,
          reviews: r.reviews,
          skills: (u.skills || []).map((s) => s.name),
          matchPct: m.pct,
          matchDesc: m.desc,
          portfolio: u.portfolio || u.github || '',
          experience: u.experience || 'Beginner',
          bio: u.bio || '',
          invited: invitedUsers.has(idStr(u)),
          requested: collabUsers.has(idStr(u)),
        };
      })
      .sort((a, b) => b.matchPct - a.matchPct);

    // ---- My projects ----
    const myProjectCards = myProjects.map((p) => {
      const tasks = tasksByProject.get(idStr(p)) || [];
      const isLead = idStr(p.owner) === meId;
      const myMember = p.members.find((m) => idStr(m.user) === meId);
      const nextTask = tasks.find((t) => t.status !== 'done' && t.dueDate);
      let nextMilestone = p.nextMilestone;
      if (p.status !== 'Completed' && nextTask) {
        const due = f.dueLabel(nextTask.dueDate, now);
        nextMilestone = `${nextTask.title} (${due.label})`;
      }
      return {
        id: idStr(p),
        title: p.title,
        category: p.category,
        description: p.description,
        myRole: (myMember && myMember.role) || (isLead ? 'Project Lead' : 'Member'),
        isLead,
        status: p.status,
        archived: p.archived,
        progress: projectProgress(p, tasks),
        skills: p.skills,
        nextMilestone: nextMilestone || 'No upcoming deadline',
        capacity: p.maxMembers,
        members: p.members
          .filter((m) => m.user)
          .map((m) => ({
            initials: f.initials(m.user.name),
            name: m.user.name,
            role: m.role,
            bg: m.user.avatarBg,
            fg: m.user.avatarFg,
          })),
        tasks: tasks.map((t) => ({
          id: idStr(t),
          title: t.title,
          due: t.dueDate ? f.shortDate(t.dueDate) : 'Upcoming',
          done: t.status === 'done',
        })),
        candidates: isLead
          ? (candidatesByProject.get(idStr(p)) || []).filter((c) => c.user).map((c) => ({
            id: idStr(c),
            name: c.user.name,
            role: c.role,
            initials: f.initials(c.user.name),
            bg: c.user.avatarBg,
            fg: c.user.avatarFg,
            matchPct: `${c.matchPct || matchFor(me, c.user).pct}%`,
          }))
          : [],
      };
    });

    // ---- My tasks (overview "Upcoming deadlines") ----
    const myOpenTasks = projectTasks
      .filter((t) => idStr(t.assignee) === meId && t.status !== 'done')
      .sort((a, b) => (a.dueDate ? a.dueDate.getTime() : Infinity) - (b.dueDate ? b.dueDate.getTime() : Infinity));
    const overviewTasks = myOpenTasks.slice(0, 8).map((t) => {
      const due = f.dueLabel(t.dueDate, now);
      return {
        id: idStr(t),
        title: t.title,
        subtitle: `${due.label} · ${t.project ? t.project.title : ''}`,
        statusType: due.statusType,
        completed: false,
      };
    });
    const dueSoon = myOpenTasks.filter((t) => t.dueDate && f.dayDiff(t.dueDate, now) <= 7);
    const overdue = myOpenTasks.filter((t) => t.dueDate && f.dayDiff(t.dueDate, now) < 0);

    // ---- Overview: recommended projects & suggested teammates ----
    const recommended = allProjects
      .filter((p) => p.status === 'Recruiting' && !p.isMember(me._id))
      .map((p) => ({ p, score: p.skills.filter((s) => mySkillSet.has(s.toLowerCase())).length }))
      .sort((a, b) => b.score - a.score)
      .slice(0, 2)
      .map(({ p }, i) => {
        const owner = p.owner || { name: 'TeamUp' };
        const open = (p.openRoles || []).length;
        return {
          id: idStr(p),
          title: p.title,
          author: `${owner.name} · ${p.category}`,
          desc: p.description,
          skills: p.skills.slice(0, 3),
          footer: `${open} open role${open === 1 ? '' : 's'} · ${p.members.length} / ${p.maxMembers} members`,
          isGreen: i % 2 === 0,
          type: i % 2,
          bookmarked: bookmarked.has(idStr(p)),
          requested: requestedProjects.has(idStr(p)),
        };
      });

    const suggested = teammates.slice(0, 2).map((t) => ({
      id: t.id,
      initials: t.initials,
      avatarBg: t.avatarBg,
      avatarFg: t.avatarFg,
      name: t.name,
      role: `${t.role}${t.university ? ` · ${f.shortUniversity(t.university)}` : ''}`,
      matchScore: `${t.matchPct}% match`,
      bio: t.bio,
      university: t.university,
      invited: t.invited,
    }));

    const overviewActivities = activities.slice(0, 6).filter((a) => a.actor).map((a) => {
      const badge = ACTIVITY_BADGES[a.type] || ACTIVITY_BADGES.task;
      return {
        initials: f.initials(a.actor.name),
        avatarBg: a.actor.avatarBg,
        avatarFg: a.actor.avatarFg,
        title: a.actor.name,
        subtitle: `${a.text} · ${f.agoLong(a.createdAt, now)}`,
        badgeText: badge[0],
        badgeBg: badge[1],
        badgeFg: badge[2],
      };
    });

    // ---- Stats ----
    const activeMine = myProjects.filter((p) => !p.archived && p.status !== 'Completed');
    const completedMine = myProjects.filter((p) => p.archived || p.status === 'Completed');
    const leadingActive = activeMine.filter((p) => idStr(p.owner) === meId).length;
    const received = candidates.length + receivedInvites;
    const sent = sentJoin.length + sentInvites.length;

    const coMembers = new Set();
    myProjects.forEach((p) => p.members.forEach((m) => {
      if (m.user && idStr(m.user) !== meId) coMembers.add(idStr(m.user));
    }));
    const myAssigned = projectTasks.filter((t) => idStr(t.assignee) === meId);
    const myDone = myAssigned.filter((t) => t.status === 'done').length;
    const myRating = ratingFor(me, ratingAgg);

    const stats = {
      activeProjects: activeMine.length,
      activeProjectsSub: `${leadingActive} leading · ${activeMine.length - leadingActive} joined`,
      myTeams: myProjects.length,
      myTeamsSub: `${activeMine.length} active · ${completedMine.length} completed`,
      pendingRequests: received + sent,
      pendingRequestsSub: `${received} received · ${sent} sent`,
      tasksDue: dueSoon.length,
      tasksDueSub: `Next 7 days · ${overdue.length} overdue`,
    };

    // ---- Team workspace (My Teams) ----
    // Primary workspace: the biggest active team I lead, else the biggest one I'm in
    const bySize = [...activeMine].sort((a, b) => b.members.length - a.members.length);
    const primary = bySize.find((p) => idStr(p.owner) === meId) || bySize[0] || null;
    const team = primary ? buildTeam(primary, tasksByProject.get(idStr(primary)) || [],
      candidatesByProject.get(idStr(primary)) || [], activities, meId, now) : null;

    // ---- Profile ----
    const handle = me.username || me.email.split('@')[0];
    const profile = {
      name: me.name,
      handle: `@${handle}`,
      headline: me.headline || me.role,
      university: me.university || 'Add your university',
      department: me.department || '',
      gradYear: me.gradYear || '',
      location: me.location || '',
      email: me.email,
      github: me.github || '',
      portfolio: me.portfolio || '',
      bio: me.bio || 'Tell teammates what you love building.',
      availabilityStatus: me.availability || 'Availability not set',
      activeProjectsCount: activeMine.length,
      totalTeammatesCount: coMembers.size,
      completionRatePercent: myAssigned.length ? Math.round((myDone * 100) / myAssigned.length) : 0,
      peerRating: myRating.rating,
      totalReviewsCount: myRating.reviews,
      projects: myProjectCards.map((p) => ({
        id: p.id,
        title: p.title,
        role: p.myRole,
        description: p.description,
        status: p.status === 'Completed'
          ? 'Completed'
          : `${p.status} · ${p.tasks.filter((t) => !t.done).length} open tasks`,
        tags: p.skills.slice(0, 4),
        teamSize: p.members.length,
        maxTeamSize: p.capacity,
      })),
      skills: (me.skills || []).map((s) => ({
        name: s.name,
        category: s.category,
        endorsements: (s.baseEndorsements || 0) + (s.endorsedBy || []).length,
        userEndorsed: (s.endorsedBy || []).some((id) => String(id) === meId),
      })),
      reviews: myReviews.filter((r) => r.reviewer).map((r) => ({
        reviewerName: r.reviewer.name,
        reviewerInitials: f.initials(r.reviewer.name),
        reviewerRole: r.reviewer.headline || 'Teammate',
        projectName: r.project ? r.project.title : '',
        feedback: r.feedback,
        rating: r.rating,
        date: f.longDate(r.createdAt),
        avatarBg: r.reviewer.avatarBg,
        avatarFg: r.reviewer.avatarFg,
      })),
    };

    const s = me.settings || {};
    const settings = {
      fullName: me.name,
      username: handle,
      email: me.email,
      passwordLastChanged: f.fullDate(me.passwordChangedAt || me.createdAt),
      memberSince: f.fullDate(me.createdAt),
      accountId: `TU-${meId.slice(-6).toUpperCase()}`,
      visibility: s.visibility || 'PUBLIC',
      showSkills: s.showSkills !== false,
      showActivity: !!s.showActivity,
      categories: s.categories || [],
      roleFrontendDev: !!s.roleFrontendDev,
      roleUiDev: !!s.roleUiDev,
      roleBackendDev: !!s.roleBackendDev,
      roleProjectLead: !!s.roleProjectLead,
      weeklyAvailability: s.weeklyAvailability || '10–15 hours per week',
      preferredCommitment: s.preferredCommitment || '1–3 months',
      collabMode: s.collabMode || 'REMOTE',
      ownedProject: (myProjects.find((p) => idStr(p.owner) === meId && !p.archived) || {}).title || '',
      supportEmail: 'support@teamup.com',
    };

    return res.status(200).json({
      success: true,
      generatedAt: now.toISOString(),
      me: {
        id: meId,
        name: me.name,
        firstName: f.firstName(me.name),
        initials: f.initials(me.name),
        email: me.email,
        role: me.role,
        bio: me.bio || '',
        portfolio: me.portfolio || '',
        profileComplete: profileCompleteness(me),
      },
      stats,
      overview: {
        projects: recommended,
        teammates: suggested,
        tasks: overviewTasks,
        activities: overviewActivities,
      },
      notifications: notifications.map((n) => `${n.text} (${f.agoCompact(n.createdAt, now)} ago)`),
      unreadNotifications: notifications.filter((n) => !n.read).length,
      discover,
      teammates,
      myProjects: myProjectCards,
      team,
      profile,
      settings,
    });
  } catch (error) {
    console.error('[Dashboard Error]:', error);
    return res.status(500).json({ success: false, message: error.message || 'Could not load dashboard.' });
  }
};

function buildTeam(p, tasks, candidates, activities, meId, now) {
  const done = tasks.filter((t) => t.status === 'done').length;
  const open = tasks.filter((t) => t.status !== 'done');
  const dueSoon = open.filter((t) => t.dueDate && f.dayDiff(t.dueDate, now) <= 7);
  const overdue = open.filter((t) => t.dueDate && f.dayDiff(t.dueDate, now) < 0);

  const milestones = [...(p.milestones || [])].sort((a, b) => (a.dueDate || 0) - (b.dueDate || 0));
  const nextMs = milestones.find((m) => m.status !== 'Completed');
  let nextMilestoneDate = '—';
  let nextMilestoneSub = 'No upcoming milestone';
  if (nextMs && nextMs.dueDate) {
    const days = f.dayDiff(nextMs.dueDate, now);
    nextMilestoneDate = f.shortDate(nextMs.dueDate);
    nextMilestoneSub = `${nextMs.title} · ${days >= 0 ? `${days} days left` : `${-days} days late`}`;
  }

  const summary = { todo: 0, inProgress: 0, inReview: 0, done: 0 };
  tasks.forEach((t) => {
    if (t.status === 'todo') summary.todo += 1;
    else if (t.status === 'in_progress') summary.inProgress += 1;
    else if (t.status === 'in_review') summary.inReview += 1;
    else summary.done += 1;
  });

  const ownerId = idStr(p.owner);
  const members = p.members.filter((m) => m.user).map((m) => ({
    name: m.user.name,
    initials: f.initials(m.user.name),
    role: m.role,
    badge: idStr(m.user) === meId ? 'You' : idStr(m.user) === ownerId ? 'Lead' : 'Member',
    avatarBg: m.user.avatarBg,
    avatarFg: m.user.avatarFg,
  }));

  const teamActivities = activities
    .filter((a) => a.actor && a.project && idStr(a.project) === idStr(p))
    .slice(0, 6)
    .map((a) => ({
      name: a.actor.name,
      initials: f.initials(a.actor.name),
      actionText: `${a.text} · ${f.agoCompact(a.createdAt, now)}`,
      avatarBg: a.actor.avatarBg,
      avatarFg: a.actor.avatarFg,
    }));

  const fmtSync = (d) => {
    const h = d.getHours();
    const hr = h % 12 === 0 ? 12 : h % 12;
    return `${f.shortDate(d)}, ${hr} ${h < 12 ? 'AM' : 'PM'}`;
  };
  let syncNotice = 'No team sync scheduled yet';
  if (p.lastSyncAt || p.nextSyncAt) {
    syncNotice = [
      p.lastSyncAt ? `Last team sync: ${f.shortDate(p.lastSyncAt)}` : null,
      p.nextSyncAt ? `Next: ${fmtSync(p.nextSyncAt)}` : null,
    ].filter(Boolean).join(' · ');
  }

  return {
    projectId: idStr(p),
    workspaceName: `${p.title} workspace`,
    subtitle: 'Your team, your progress, your next big thing.',
    breadcrumbs: `My Teams / ${p.title} / Workspace`,
    progressPercent: projectProgress(p, tasks),
    tasksCompleted: done,
    tasksTotal: tasks.length,
    currentMembers: members.length,
    maxMembers: p.maxMembers,
    openPositions: Math.max(0, p.maxMembers - members.length),
    nextMilestoneDate,
    nextMilestoneSub,
    tasksDueSoon: dueSoon.length,
    tasksDueSoonSub: overdue.length ? `${overdue.length} overdue · Needs attention` : 'All on track',
    tabs: ['Overview', `Members (${members.length})`, `Tasks (${tasks.length})`, 'Files', `Requests (${candidates.length})`],
    members,
    milestones: milestones.map((m) => ({
      title: m.title,
      dueInfo: `Due ${m.dueDate ? f.shortDate(m.dueDate) : 'TBD'} · ${m.status}`,
      progress: m.progress,
      status: m.status,
    })),
    taskSummary: summary,
    requests: candidates.filter((c) => c.user).map((c) => ({
      id: idStr(c),
      name: c.user.name,
      initials: f.initials(c.user.name),
      roleSkill: `${c.role}${c.user.skills && c.user.skills[0] ? ` · ${c.user.skills[0].name}` : ''}`,
      avatarBg: c.user.avatarBg,
      avatarFg: c.user.avatarFg,
    })),
    pendingRequestsCount: candidates.length,
    activities: teamActivities,
    syncNotice,
    files: (p.files || []).map((fl) => ({
      name: fl.name,
      type: fl.type,
      size: fl.size,
      updatedAgo: f.agoLong(fl.updatedAt, now),
      author: fl.authorName || '',
    })),
  };
}

module.exports = { getDashboard, matchFor };
