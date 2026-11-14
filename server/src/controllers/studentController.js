const User = require('../models/User');
const Project = require('../models/Project');
const Task = require('../models/Task');
const JoinRequest = require('../models/JoinRequest');
const Invitation = require('../models/Invitation');
const Activity = require('../models/Activity');
const Notification = require('../models/Notification');
const { matchFor } = require('./dashboardController');
const f = require('../utils/format');

const idStr = (v) => String((v && v._id) || v);

const CATEGORY_ICONS = {
  'campus life': [0, '#FDEEF4', '#EA4C89'],
  sustainability: [1, '#EBFBF5', '#10B981'],
  research: [2, '#F3F0FF', '#7C5CFF'],
  'ai & research': [2, '#F3F0FF', '#7C5CFF'],
  'social impact': [3, '#E0F2FE', '#0EA5E9'],
  'design & ux': [3, '#E0F2FE', '#0EA5E9'],
  education: [4, '#EEF2FF', '#4F46E5'],
};
const DEFAULT_ICON = [6, '#EEF2FF', '#4F46E5'];

const bad = (res, message, status = 400) => res.status(status).json({ success: false, message });
const fail = (res, error, label) => {
  console.error(`[${label}]`, error);
  return res.status(500).json({ success: false, message: error.message || 'Server error' });
};

/** body.active (boolean) wins; otherwise toggle the current state. */
const desired = (body, current) => (typeof (body && body.active) === 'boolean' ? body.active : !current);

const cleanStr = (v, max) => (typeof v === 'string' ? v.trim().slice(0, max) : '');
const cleanList = (v, maxItems, maxLen) =>
  Array.isArray(v)
    ? [...new Set(v.filter((s) => typeof s === 'string' && s.trim()).map((s) => s.trim().slice(0, maxLen)))].slice(0, maxItems)
    : [];

async function loadProject(req, res) {
  if (!f.isId(req.params.id)) {
    bad(res, 'Invalid project id.');
    return null;
  }
  const project = await Project.findById(req.params.id);
  if (!project) {
    bad(res, 'Project not found.', 404);
    return null;
  }
  return project;
}

const notify = (userId, text) => Notification.create({ user: userId, text });

// ---------------------------------------------------------------------------
// Projects
// ---------------------------------------------------------------------------

/** POST /api/projects */
const createProject = async (req, res) => {
  try {
    const title = cleanStr(req.body.title, 80);
    if (!title) return bad(res, 'Project title is required.');
    const category = cleanStr(req.body.category, 40) || 'Campus life';
    const skills = cleanList(req.body.skills, 10, 30);
    const openRoles = cleanList(req.body.openRoles, 6, 40);
    const maxMembers = Math.min(20, Math.max(2, parseInt(req.body.maxMembers, 10) || 4));
    const [iconType, iconBg, iconColor] = CATEGORY_ICONS[category.toLowerCase()] || DEFAULT_ICON;

    const project = await Project.create({
      title,
      description: cleanStr(req.body.description, 600),
      category,
      experience: ['Beginner', 'Intermediate', 'Advanced'].includes(req.body.experience) ? req.body.experience : 'Intermediate',
      skills,
      openRoles: openRoles.length ? openRoles : ['Developer', 'Designer', 'Researcher'],
      maxMembers,
      status: 'Recruiting',
      iconType,
      iconBg,
      iconColor,
      owner: req.user._id,
      members: [{ user: req.user._id, role: 'Project Lead' }],
    });
    await Activity.create({ project: project._id, actor: req.user._id, type: 'created', text: `Created ${title}` });

    return res.status(201).json({ success: true, message: `Project '${title}' published!`, project: { id: idStr(project), title } });
  } catch (error) {
    return fail(res, error, 'Create Project');
  }
};

/** POST /api/projects/:id/bookmark  { active?: boolean } */
const toggleBookmark = async (req, res) => {
  try {
    const project = await loadProject(req, res);
    if (!project) return undefined;
    const me = await User.findById(req.user._id).select('bookmarks');
    const has = me.bookmarks.some((b) => String(b) === idStr(project));
    const want = desired(req.body, has);
    await User.updateOne({ _id: me._id }, want ? { $addToSet: { bookmarks: project._id } } : { $pull: { bookmarks: project._id } });
    return res.json({ success: true, bookmarked: want });
  } catch (error) {
    return fail(res, error, 'Bookmark');
  }
};

/** POST /api/projects/:id/join  { role?, message? } */
const requestJoin = async (req, res) => {
  try {
    const project = await loadProject(req, res);
    if (!project) return undefined;
    if (project.isMember(req.user._id)) return bad(res, `You're already a member of ${project.title}.`);
    if (project.status !== 'Recruiting') return bad(res, `${project.title} is not recruiting right now.`);

    const existing = await JoinRequest.findOne({ project: project._id, user: req.user._id });
    if (existing && existing.status === 'pending') {
      return res.json({ success: true, message: `Request to join '${project.title}' is already pending.`, requestId: idStr(existing) });
    }

    const me = await User.findById(req.user._id).select('name skills headline');
    const owner = await User.findById(project.owner).select('skills');
    const role = cleanStr(req.body.role, 60) || (project.openRoles && project.openRoles[0]) || me.headline || 'Contributor';
    const matchPct = owner ? matchFor(owner, me).pct : 0;

    const jr = existing || new JoinRequest({ project: project._id, user: req.user._id });
    Object.assign(jr, { role, matchPct, message: cleanStr(req.body.message, 300), status: 'pending', decidedAt: undefined });
    await jr.save();
    await notify(project.owner, `${me.name} requested to join ${project.title}`);

    return res.status(201).json({ success: true, message: `Request to join '${project.title}' submitted!`, requestId: idStr(jr) });
  } catch (error) {
    return fail(res, error, 'Join Request');
  }
};

/** POST /api/projects/:id/tasks  { title, dueDate? } */
const addTask = async (req, res) => {
  try {
    const project = await loadProject(req, res);
    if (!project) return undefined;
    if (!project.isMember(req.user._id)) return bad(res, 'Only team members can add tasks.', 403);
    const title = cleanStr(req.body.title, 140);
    if (!title) return bad(res, 'Task title is required.');

    let dueDate;
    if (req.body.dueDate) {
      dueDate = new Date(req.body.dueDate);
      if (Number.isNaN(dueDate.getTime())) dueDate = undefined;
    }
    const task = await Task.create({
      project: project._id,
      title,
      dueDate,
      assignee: req.user._id,
      createdBy: req.user._id,
    });
    await Activity.create({ project: project._id, actor: req.user._id, type: 'task', text: `Added task "${title}"` });
    project.updatedAt = new Date();
    await project.save();

    return res.status(201).json({
      success: true,
      task: { id: idStr(task), title: task.title, due: dueDate ? f.shortDate(dueDate) : 'Upcoming', done: false },
    });
  } catch (error) {
    return fail(res, error, 'Add Task');
  }
};

/** POST /api/tasks/:id/toggle  { done?: boolean } */
const toggleTask = async (req, res) => {
  try {
    if (!f.isId(req.params.id)) return bad(res, 'Invalid task id.');
    const task = await Task.findById(req.params.id);
    if (!task) return bad(res, 'Task not found.', 404);
    const project = await Project.findById(task.project);
    if (!project || !project.isMember(req.user._id)) return bad(res, 'Only team members can update this task.', 403);

    const isDone = task.status === 'done';
    const wantDone = typeof req.body.done === 'boolean' ? req.body.done : !isDone;
    if (wantDone !== isDone) {
      task.status = wantDone ? 'done' : 'todo';
      task.completedAt = wantDone ? new Date() : undefined;
      await task.save();
      if (wantDone) {
        await Activity.create({ project: project._id, actor: req.user._id, type: 'completed', text: `Completed "${task.title}"` });
      }
    }

    const all = await Task.find({ project: project._id }).select('status');
    const done = all.filter((t) => t.status === 'done').length;
    return res.json({ success: true, done: wantDone, progress: all.length ? Math.round((done * 100) / all.length) : 0 });
  } catch (error) {
    return fail(res, error, 'Toggle Task');
  }
};

/** POST /api/requests/:id/accept  |  /api/requests/:id/decline */
const decideRequest = (accept) => async (req, res) => {
  try {
    if (!f.isId(req.params.id)) return bad(res, 'Invalid request id.');
    const jr = await JoinRequest.findById(req.params.id).populate('user', 'name');
    if (!jr) return bad(res, 'Request not found.', 404);
    const project = await Project.findById(jr.project);
    if (!project) return bad(res, 'Project not found.', 404);
    if (idStr(project.owner) !== idStr(req.user._id)) return bad(res, 'Only the project lead can review requests.', 403);
    if (jr.status !== 'pending') return bad(res, `This request was already ${jr.status}.`);

    jr.status = accept ? 'accepted' : 'declined';
    jr.decidedAt = new Date();
    await jr.save();

    if (accept && !project.isMember(jr.user._id)) {
      project.members.push({ user: jr.user._id, role: jr.role });
      if (project.members.length >= project.maxMembers) project.status = 'Active';
      await project.save();
      await Activity.create({ project: project._id, actor: jr.user._id, type: 'joined', text: `Joined ${project.title} as ${jr.role}` });
    }
    await notify(jr.user._id, accept
      ? `Your request to join ${project.title} was accepted 🎉`
      : `Your request to join ${project.title} was declined`);

    return res.json({ success: true, status: jr.status, members: project.members.length });
  } catch (error) {
    return fail(res, error, 'Decide Request');
  }
};

// ---------------------------------------------------------------------------
// People
// ---------------------------------------------------------------------------

/** POST /api/users/:id/invite | /api/users/:id/collab   { active?: boolean, projectId? } */
const setOutreach = (kind) => async (req, res) => {
  try {
    if (!f.isId(req.params.id)) return bad(res, 'Invalid user id.');
    if (idStr(req.params.id) === idStr(req.user._id)) return bad(res, "You can't invite yourself.");
    const target = await User.findById(req.params.id).select('name');
    if (!target) return bad(res, 'User not found.', 404);

    const existing = await Invitation.findOne({ from: req.user._id, to: target._id, kind });
    const isActive = !!existing && existing.status === 'pending';
    const want = desired(req.body, isActive);

    if (want && !isActive) {
      let project;
      if (req.body.projectId && f.isId(req.body.projectId)) {
        project = await Project.findOne({ _id: req.body.projectId, owner: req.user._id }).select('_id title');
      }
      await Invitation.findOneAndUpdate(
        { from: req.user._id, to: target._id, kind },
        { status: 'pending', project: project ? project._id : undefined },
        { upsert: true, new: true, setDefaultsOnInsert: true }
      );
      const me = await User.findById(req.user._id).select('name');
      await notify(target._id, kind === 'invite'
        ? `${me.name} invited you to collaborate${project ? ` on ${project.title}` : ''}`
        : `${me.name} wants to collaborate with you`);
    } else if (!want && existing) {
      await Invitation.deleteOne({ _id: existing._id });
    }
    return res.json({ success: true, active: want, name: target.name });
  } catch (error) {
    return fail(res, error, 'Outreach');
  }
};

// ---------------------------------------------------------------------------
// Me: profile, settings, endorsements, notifications
// ---------------------------------------------------------------------------

const PROFILE_FIELDS = {
  name: 80, headline: 80, university: 100, department: 120, gradYear: 30, location: 80,
  bio: 600, github: 120, portfolio: 120, availability: 60,
};

/** PUT /api/me/profile */
const updateProfile = async (req, res) => {
  try {
    const me = await User.findById(req.user._id);
    for (const [key, max] of Object.entries(PROFILE_FIELDS)) {
      if (typeof req.body[key] === 'string') me[key] = req.body[key].trim().slice(0, max);
    }
    if (typeof req.body.availabilityStatus === 'string' && !req.body.availability) {
      me.availability = req.body.availabilityStatus.trim().slice(0, 60);
    }
    if (!me.name) return bad(res, 'Name cannot be empty.');
    if (['Beginner', 'Intermediate', 'Advanced'].includes(req.body.experience)) me.experience = req.body.experience;
    if (Array.isArray(req.body.skills)) {
      const existing = new Map(me.skills.map((s) => [s.name.toLowerCase(), s]));
      me.skills = cleanList(req.body.skills, 20, 30).map((name) => existing.get(name.toLowerCase()) || { name, category: 'Tools' });
    }
    await me.save();
    return res.json({ success: true, message: 'Profile updated.' });
  } catch (error) {
    return fail(res, error, 'Update Profile');
  }
};

const ENUMS = {
  visibility: ['PUBLIC', 'TEAM_MEMBERS', 'PRIVATE'],
  collabMode: ['REMOTE', 'IN_PERSON', 'EITHER'],
};
const SETTING_BOOLS = ['showSkills', 'showActivity', 'roleFrontendDev', 'roleUiDev', 'roleBackendDev', 'roleProjectLead'];

/** PUT /api/me/settings */
const updateSettings = async (req, res) => {
  try {
    const b = req.body || {};
    const me = await User.findById(req.user._id);

    if (typeof b.fullName === 'string') {
      const name = b.fullName.trim().slice(0, 80);
      if (!name) return bad(res, 'Full name cannot be empty.');
      me.name = name;
    }
    if (typeof b.username === 'string' && b.username.trim()) {
      const username = b.username.trim().toLowerCase().replace(/^@/, '');
      if (!/^[a-z0-9._-]{3,30}$/.test(username)) return bad(res, 'Username must be 3-30 letters, numbers, dots, dashes or underscores.');
      const taken = await User.exists({ username, _id: { $ne: me._id } });
      if (taken) return bad(res, 'That username is already taken.');
      me.username = username;
    }
    if (typeof b.email === 'string' && b.email.trim()) {
      const email = b.email.trim().toLowerCase();
      if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) return bad(res, 'Please enter a valid email address.');
      const taken = await User.exists({ email, _id: { $ne: me._id } });
      if (taken) return bad(res, 'An account with this email already exists.');
      me.email = email;
    }

    const s = me.settings || {};
    for (const key of SETTING_BOOLS) if (typeof b[key] === 'boolean') s[key] = b[key];
    for (const [key, allowed] of Object.entries(ENUMS)) if (allowed.includes(b[key])) s[key] = b[key];
    if (typeof b.weeklyAvailability === 'string') s.weeklyAvailability = b.weeklyAvailability.slice(0, 40);
    if (typeof b.preferredCommitment === 'string') s.preferredCommitment = b.preferredCommitment.slice(0, 40);
    if (Array.isArray(b.categories)) s.categories = cleanList(b.categories, 12, 40);
    me.settings = s;
    me.markModified('settings');

    await me.save();
    return res.json({ success: true, message: 'Settings saved.' });
  } catch (error) {
    return fail(res, error, 'Update Settings');
  }
};

/** POST /api/me/skills/:name/endorse  { active?: boolean } */
const toggleEndorsement = async (req, res) => {
  try {
    const me = await User.findById(req.user._id);
    const skill = me.skills.find((s) => s.name.toLowerCase() === String(req.params.name).toLowerCase());
    if (!skill) return bad(res, 'Skill not found on your profile.', 404);
    const has = skill.endorsedBy.some((id) => String(id) === idStr(me));
    const want = desired(req.body, has);
    if (want && !has) skill.endorsedBy.push(me._id);
    if (!want && has) skill.endorsedBy = skill.endorsedBy.filter((id) => String(id) !== idStr(me));
    await me.save();
    return res.json({
      success: true,
      endorsed: want,
      endorsements: (skill.baseEndorsements || 0) + skill.endorsedBy.length,
    });
  } catch (error) {
    return fail(res, error, 'Endorse');
  }
};

/** POST /api/notifications/read */
const markNotificationsRead = async (req, res) => {
  try {
    await Notification.updateMany({ user: req.user._id, read: false }, { read: true });
    return res.json({ success: true });
  } catch (error) {
    return fail(res, error, 'Notifications');
  }
};

module.exports = {
  createProject,
  toggleBookmark,
  requestJoin,
  addTask,
  toggleTask,
  acceptRequest: decideRequest(true),
  declineRequest: decideRequest(false),
  inviteUser: setOutreach('invite'),
  collabUser: setOutreach('collab'),
  updateProfile,
  updateSettings,
  toggleEndorsement,
  markNotificationsRead,
};
