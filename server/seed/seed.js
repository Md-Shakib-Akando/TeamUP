/**
 * TeamUp demo-data seeder.
 *
 *   npm run seed                         # insert demo data (skips if already seeded)
 *   npm run seed -- --reset              # delete demo data, then insert it again
 *   npm run seed -- --clean              # only delete demo data
 *   npm run seed -- --attach=you@x.com   # also add an existing account to demo teams/tasks
 *   npm run seed -- --attach-all         # ...for every non-demo, non-admin account
 *   npm run seed -- --import-users-from=certchain
 *        # copy TeamUp accounts (role Student/Developer/...) that were registered into another
 *        # database on the same cluster before TeamUp had its own database
 *
 * Demo accounts all use the password "teamup123". The persona the dashboard was designed
 * around is alex.morgan@example.com.
 */
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '..', '.env') });
const mongoose = require('mongoose');

const connectDB = require('../src/config/db');
const User = require('../src/models/User');
const Project = require('../src/models/Project');
const Task = require('../src/models/Task');
const JoinRequest = require('../src/models/JoinRequest');
const Invitation = require('../src/models/Invitation');
const Activity = require('../src/models/Activity');
const Notification = require('../src/models/Notification');
const Review = require('../src/models/Review');
const D = require('./demoData');

const DAY = 24 * 60 * 60 * 1000;
const MIN = 60 * 1000;

const args = process.argv.slice(2);
const flag = (name) => args.includes(`--${name}`);
const option = (name) => {
  const hit = args.find((a) => a.startsWith(`--${name}=`));
  return hit ? hit.slice(name.length + 3) : null;
};

const daysFromNow = (d, hour = 17) => {
  const t = new Date(Date.now() + d * DAY);
  t.setHours(hour, 0, 0, 0);
  return t;
};
const minutesAgo = (m) => new Date(Date.now() - m * MIN);
const emailFor = (name) => `${name.toLowerCase().replace(/[^a-z]+/g, '.').replace(/^\.|\.$/g, '')}@example.com`;
const TEAMUP_ROLES = ['Student', 'Developer', 'UI/UX Designer', 'Team Leader', 'Researcher', 'Freelancer', 'Admin'];

async function clean() {
  const demoUsers = await User.find({ isDemo: true }).select('_id');
  const u = demoUsers.map((x) => x._id);
  // Seeded projects plus anything a demo account created while you were trying the app
  const demoProjects = await Project.find({ $or: [{ isDemo: true }, { owner: { $in: u } }] }).select('_id');
  const p = demoProjects.map((x) => x._id);

  const results = await Promise.all([
    Task.deleteMany({ project: { $in: p } }),
    JoinRequest.deleteMany({ $or: [{ project: { $in: p } }, { user: { $in: u } }] }),
    Invitation.deleteMany({ $or: [{ from: { $in: u } }, { to: { $in: u } }] }),
    Activity.deleteMany({ $or: [{ project: { $in: p } }, { actor: { $in: u } }] }),
    Notification.deleteMany({ $or: [{ user: { $in: u } }, { isDemo: true }] }),
    Review.deleteMany({ $or: [{ reviewer: { $in: u } }, { reviewee: { $in: u } }] }),
  ]);
  await User.updateMany({}, { $pull: { bookmarks: { $in: p } } });
  await Project.deleteMany({ _id: { $in: p } });
  await User.deleteMany({ _id: { $in: u } });
  console.log(`[seed] Removed ${u.length} demo users, ${p.length} demo projects, ` +
    `${results.reduce((n, r) => n + (r.deletedCount || 0), 0)} related documents.`);
}

async function importUsers(fromDb) {
  const source = mongoose.connection.client.db(fromDb).collection('users');
  const target = mongoose.connection.db.collection('users');
  const candidates = await source.find({ role: { $in: TEAMUP_ROLES }, password: { $type: 'string' } }).toArray();
  let copied = 0;
  for (const doc of candidates) {
    const exists = await target.findOne({ $or: [{ _id: doc._id }, { email: doc.email }] });
    if (exists) continue;
    const { _id, name, email, password, role, createdAt, updatedAt } = doc;
    await target.insertOne({ _id, name, email, password, role, createdAt, updatedAt, isDemo: false, skills: [], bookmarks: [] });
    copied += 1;
  }
  console.log(`[seed] Imported ${copied} TeamUp account(s) from "${fromDb}" (${candidates.length - copied} already present). ` +
    `The originals in "${fromDb}" were left untouched.`);
}

async function seed() {
  const users = {};

  // ---- Users ----
  const persona = D.PERSONA;
  users[persona.key] = await User.create({
    name: persona.name,
    email: persona.email,
    username: persona.username,
    password: D.DEMO_PASSWORD,
    role: 'Student',
    headline: persona.headline,
    university: persona.university,
    department: persona.department,
    gradYear: persona.gradYear,
    location: persona.location,
    github: persona.github,
    portfolio: persona.portfolio,
    bio: persona.bio,
    availability: persona.availability,
    experience: persona.experience,
    avatarBg: persona.avatar[0],
    avatarFg: persona.avatar[1],
    ratingBase: persona.rating,
    reviewsBase: persona.reviews,
    skills: persona.skills.map(([name, category, n]) => ({ name, category, baseEndorsements: n })),
    settings: persona.settings,
    isDemo: true,
  });
  const memberSince = new Date(Date.now() - persona.memberSinceDaysAgo * DAY);
  await User.collection.updateOne(
    { _id: users[persona.key]._id },
    { $set: { createdAt: memberSince, passwordChangedAt: new Date(Date.now() - 62 * DAY) } }
  );

  for (const t of D.TEAMMATES) {
    const [key, name, headline, university, avatar, availability, rating, reviews, skills, matchHint, matchDesc,
      portfolio, experience, bio] = t;
    const email = key === 'priya' ? 'priyashah@example.com' : key === 'noah' ? 'noah.w@example.com' : emailFor(name);
    users[key] = await User.create({
      name,
      email,
      username: email.split('@')[0].replace(/[^a-z0-9._-]/g, ''),
      password: D.DEMO_PASSWORD,
      role: 'Student',
      headline,
      university,
      availability,
      experience,
      bio,
      portfolio,
      avatarBg: avatar[0],
      avatarFg: avatar[1],
      ratingBase: rating,
      reviewsBase: reviews,
      matchHint,
      matchDesc,
      skills: skills.map((s) => ({ name: s, category: D.categoryOf(s), baseEndorsements: Math.max(2, Math.round(reviews * 0.8)) })),
      isDemo: true,
    });
  }
  console.log(`[seed] Created ${Object.keys(users).length} demo users (password "${D.DEMO_PASSWORD}").`);

  // ---- Projects ----
  const projects = {};
  const pool = D.TEAMMATES.map((t) => t[0]).filter((k) => !['alexr', 'elenav', 'amara'].includes(k));
  let cursor = 0;
  for (const p of D.PROJECTS) {
    const members = (p.members || [[p.owner, 'Project Lead']]).map(([k, role]) => ({ user: users[k]._id, role }));
    const used = new Set((p.members || [[p.owner]]).map(([k]) => k));
    while (members.length < p.current) {
      const k = pool[cursor % pool.length];
      cursor += 1;
      if (used.has(k)) continue;
      used.add(k);
      members.push({ user: users[k]._id, role: D.TEAMMATES.find((t) => t[0] === k)[2] });
    }
    projects[p.key] = await Project.create({
      title: p.title,
      category: p.category,
      experience: p.experience,
      description: p.description,
      skills: p.skills,
      status: p.status,
      archived: !!p.archived,
      maxMembers: p.max,
      openRoles: p.openRoles,
      iconType: p.icon[0],
      iconBg: p.icon[1],
      iconColor: p.icon[2],
      nextMilestone: p.nextMilestone || '',
      owner: users[p.owner]._id,
      members,
      milestones: (p.milestones || []).map(([title, days, status, progress]) => ({
        title, dueDate: daysFromNow(days), status, progress,
      })),
      files: (p.files || []).map(([name, type, size, days, authorName]) => ({
        name, type, size, authorName, updatedAt: new Date(Date.now() + days * DAY),
      })),
      lastSyncAt: p.lastSyncDays != null ? daysFromNow(p.lastSyncDays, 16) : undefined,
      nextSyncAt: p.nextSyncDays != null ? daysFromNow(p.nextSyncDays, 16) : undefined,
      isDemo: true,
    });
  }
  // Stagger creation dates so "newest first" lists keep the original demo order
  let age = 0;
  for (const p of D.PROJECTS) {
    age += 3;
    const ts = new Date(Date.now() - age * DAY);
    await Project.collection.updateOne({ _id: projects[p.key]._id }, { $set: { createdAt: ts, updatedAt: ts } });
  }
  // Persona bookmarked one project in the original demo
  await User.updateOne({ _id: users.alex._id }, { $addToSet: { bookmarks: projects.foodloop._id } });
  console.log(`[seed] Created ${Object.keys(projects).length} projects.`);

  // ---- Tasks ----
  await Task.insertMany(D.TASKS.map(([pk, title, days, status, who]) => ({
    project: projects[pk]._id,
    title,
    dueDate: daysFromNow(days),
    status,
    assignee: users[who]._id,
    createdBy: projects[pk].owner,
    completedAt: status === 'done' ? daysFromNow(days) : undefined,
  })));

  // ---- Requests, invitations, activity, notifications, reviews ----
  await JoinRequest.insertMany(D.JOIN_REQUESTS.map(([pk, who, role, matchPct, days]) => ({
    project: projects[pk]._id, user: users[who]._id, role, matchPct, status: 'pending',
    createdAt: new Date(Date.now() - days * DAY),
  })));
  await Invitation.insertMany(D.INVITATIONS.map(([from, to, kind]) => ({
    from: users[from]._id, to: users[to]._id, kind, status: 'pending',
  })));
  await Activity.insertMany(D.ACTIVITIES.map(([pk, who, type, text, mins]) => ({
    project: projects[pk]._id, actor: users[who]._id, type, text, createdAt: minutesAgo(mins),
  })));
  await Notification.insertMany(D.PERSONA_NOTIFICATIONS.map(([text, mins]) => ({
    user: users.alex._id, text, createdAt: minutesAgo(mins),
  })));
  await Review.insertMany(D.REVIEWS.map(([who, pk, rating, feedback, days]) => ({
    reviewer: users[who]._id, reviewee: users.alex._id, project: projects[pk]._id, rating, feedback,
    createdAt: new Date(Date.now() - days * DAY),
  })));
  console.log(`[seed] Created ${D.TASKS.length} tasks, ${D.JOIN_REQUESTS.length} join requests, ` +
    `${D.ACTIVITIES.length} activities, ${D.REVIEWS.length} reviews.`);
}

async function attach(selector) {
  const projects = {};
  for (const p of await Project.find({ isDemo: true, title: { $in: ['CampusConnect', 'GreenRoute'] } })) {
    projects[p.title === 'CampusConnect' ? 'campusconnect' : 'greenroute'] = p;
  }
  if (!projects.campusconnect || !projects.greenroute) {
    console.warn('[seed] Demo projects not found — run the seed first.');
    return;
  }

  const query = { isDemo: { $ne: true }, role: { $ne: 'Admin' } };
  if (selector !== 'all') query.email = { $in: selector.split(',').map((e) => e.trim().toLowerCase()) };
  const accounts = await User.find(query);
  if (!accounts.length) {
    console.warn(`[seed] No matching accounts to attach (${selector}).`);
    return;
  }

  const actor = await User.findOne({ isDemo: true, email: 'priyashah@example.com' });
  for (const u of accounts) {
    for (const [pk, role] of D.ATTACH.memberships) {
      const p = projects[pk];
      if (p.isMember(u._id)) continue;
      p.members.push({ user: u._id, role });
      if (p.members.length > p.maxMembers) p.maxMembers = p.members.length;
      await p.save();
    }
    const already = await Task.exists({ assignee: u._id, project: projects.campusconnect._id });
    if (!already) {
      await Task.insertMany(D.ATTACH.tasks.map(([pk, title, days, status]) => ({
        project: projects[pk]._id, title, dueDate: daysFromNow(days), status, assignee: u._id,
        createdBy: projects[pk].owner,
      })));
      await Notification.insertMany(D.ATTACH.notifications.map(([text, mins]) => ({
        user: u._id, text, isDemo: true, createdAt: minutesAgo(mins),
      })));
      if (actor) {
        await Activity.create({
          project: projects.campusconnect._id, actor: actor._id, type: 'task',
          text: `Assigned you "${D.ATTACH.tasks[0][1]}"`, createdAt: minutesAgo(25),
        });
      }
    }
    console.log(`[seed] Attached ${u.email} to CampusConnect & GreenRoute with starter tasks.`);
  }
}

(async () => {
  const conn = await connectDB();
  if (!conn) process.exit(1);
  try {
    const importFrom = option('import-users-from');
    if (importFrom) await importUsers(importFrom);

    if (flag('reset') || flag('clean')) await clean();

    if (!flag('clean')) {
      const already = await User.exists({ isDemo: true, email: D.PERSONA.email });
      if (already) {
        console.log('[seed] Demo data already present — skipping (use --reset to recreate it).');
      } else {
        await seed();
      }
      const attachSel = flag('attach-all') ? 'all' : option('attach');
      if (attachSel) await attach(attachSel);
    }
    console.log(`[seed] Done. Database: ${mongoose.connection.name}`);
  } catch (err) {
    console.error('[seed] Failed:', err);
    process.exitCode = 1;
  } finally {
    await mongoose.disconnect();
  }
})();
