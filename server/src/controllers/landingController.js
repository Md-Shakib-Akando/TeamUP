const User = require('../models/User');
const Project = require('../models/Project');
const Review = require('../models/Review');
const Report = require('../models/Report');
const JoinRequest = require('../models/JoinRequest');
const f = require('../utils/format');
const { ratingFor } = require('./dashboardController');
const { teamCounts } = require('../services/adminStats');

/**
 * Public (no login) endpoints that power the landing page with live database data.
 * Only users whose profile visibility is PUBLIC (and who show their skills) are exposed,
 * with a shortened name and no contact details.
 */

const idStr = (v) => String((v && v._id) || v);

/** "Sarah Lin" -> "Sarah L." (public pages never show full names). */
const publicName = (name = '') => {
  const words = String(name).trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) return 'TeamUp member';
  if (words.length === 1) return words[0];
  return `${words[0]} ${words[words.length - 1][0].toUpperCase()}.`;
};

const PUBLIC_PROFILE = {
  status: 'Active',
  role: { $ne: 'Admin' },
  'settings.visibility': { $nin: ['PRIVATE', 'TEAM_MEMBERS'] },
  'settings.showSkills': { $ne: false },
};

/**
 * GET /api/platform-stats — numbers strip on the landing page.
 *  verifiedMembers: active (not suspended) accounts, admins excluded
 *  activeTeams:     ongoing projects (not completed / archived) with 2+ members — same rule as the admin overview
 */
const getPlatformStats = async (req, res) => {
  try {
    const [verifiedMembers, projects, universities, reviews, totalReports, ghostReports] = await Promise.all([
      User.countDocuments({ status: 'Active', role: { $ne: 'Admin' } }),
      Project.find({ archived: { $ne: true }, status: { $ne: 'Completed' } }).select('members maxMembers teamSize').lean(),
      User.distinct('university', { status: 'Active' }),
      Review.find().select('rating').lean(),
      Report.countDocuments(),
      Report.countDocuments({ $or: [{ category: /ghost|inactiv|drop|absent/i }, { title: /ghost|inactiv|drop|absent/i }] }),
    ]);

    const activeTeams = projects.filter((p) => teamCounts(p).members >= 2).length;
    const universitiesCount = universities.filter((u) => u && String(u).trim()).length;

    // Match accuracy: average peer rating as a share of 5 stars; falls back to average join-request match.
    let matchAccuracy = null;
    if (reviews.length > 0) {
      const avg = reviews.reduce((s, r) => s + (r.rating || 0), 0) / reviews.length;
      matchAccuracy = Math.round((avg / 5) * 100);
    } else {
      const jr = await JoinRequest.find({ matchPct: { $gt: 0 } }).select('matchPct').lean();
      if (jr.length > 0) matchAccuracy = Math.round(jr.reduce((s, j) => s + j.matchPct, 0) / jr.length);
    }
    const ghostingRate = verifiedMembers > 0 && totalReports > 0 ? Math.round((ghostReports / verifiedMembers) * 1000) / 10 : 0;

    return res.json({
      success: true,
      data: {
        verifiedMembers,
        activeTeams,
        matchAccuracy,
        ghostingRate,
        universitiesCount,
        subUniversities: universitiesCount > 0
          ? `Across ${universitiesCount} ${universitiesCount === 1 ? 'university' : 'universities'}`
          : 'Students on TeamUp',
        generatedAt: new Date().toISOString(),
      },
    });
  } catch (error) {
    console.error('[Platform Stats Error]:', error);
    return res.status(500).json({ success: false, message: 'Failed to fetch platform stats' });
  }
};

/**
 * GET /api/match-demo — data for the landing page "See matching in real time" section:
 *  demandSkills: skills most requested by recruiting projects (pre-selected on the page).
 *                No individual project is named — project titles stay inside the app.
 *  skills:       chips = top requested skills + the most common skills on public profiles (max 8)
 *  candidates:   public student profiles with skills (ranked on the client as chips are toggled)
 */
const getMatchDemo = async (req, res) => {
  try {
    const [demandAgg, users, reviewAgg, completedAgg, totalPublic, recruitingProjects] = await Promise.all([
      Project.aggregate([
        { $match: { status: 'Recruiting', archived: { $ne: true } } },
        { $unwind: '$skills' },
        { $group: { _id: { $toLower: { $trim: { input: '$skills' } } }, name: { $first: '$skills' }, n: { $sum: 1 } } },
        { $sort: { n: -1, _id: 1 } },
        { $limit: 6 },
      ]),
      User.find({ ...PUBLIC_PROFILE, 'skills.0': { $exists: true } })
        .sort({ updatedAt: -1 }).limit(60)
        .select('name headline university availability skills bio avatarBg avatarFg ratingBase reviewsBase')
        .lean(),
      Review.aggregate([{ $group: { _id: '$reviewee', sum: { $sum: '$rating' }, count: { $sum: 1 } } }]),
      Project.aggregate([
        { $match: { status: 'Completed' } },
        { $unwind: '$members' },
        { $group: { _id: '$members.user', n: { $sum: 1 } } },
      ]),
      User.countDocuments({ ...PUBLIC_PROFILE, 'skills.0': { $exists: true } }),
      Project.countDocuments({ status: 'Recruiting', archived: { $ne: true } }),
    ]);
    const demandSkills = demandAgg.filter((d) => d._id).map((d) => ({ name: String(d.name).trim(), projects: d.n }));

    const ratingAgg = new Map(reviewAgg.map((r) => [String(r._id), r]));
    const completed = new Map(completedAgg.map((r) => [String(r._id), r.n]));

    // Skill chips: most requested skills first, then the most common profile skills
    const freq = new Map();
    for (const u of users) {
      for (const s of u.skills || []) {
        const key = String(s.name || '').trim().toLowerCase();
        if (!key) continue;
        const e = freq.get(key) || { name: String(s.name).trim(), n: 0 };
        e.n += 1;
        freq.set(key, e);
      }
    }
    const chips = [];
    const seen = new Set();
    const push = (name) => {
      const key = String(name || '').trim().toLowerCase();
      if (!key || seen.has(key) || chips.length >= 8) return;
      seen.add(key);
      chips.push((freq.get(key) || { name: String(name).trim() }).name);
    };
    for (const d of demandSkills) if (chips.length < 4) push(d.name);
    [...freq.values()].sort((a, b) => b.n - a.n || a.name.localeCompare(b.name)).forEach((e) => push(e.name));

    const candidates = users.map((u) => {
      const r = ratingFor(u, ratingAgg);
      const [bg, fg] = u.avatarBg && u.avatarFg ? [u.avatarBg, u.avatarFg] : f.paletteFor(u.name);
      return {
        id: idStr(u),
        name: publicName(u.name),
        initials: f.initials(u.name),
        role: (u.headline || '').trim() || 'Student',
        university: (u.university || '').trim(),
        availability: (u.availability || '').trim(),
        bio: String(u.bio || '').trim().slice(0, 220),
        skills: (u.skills || []).map((s) => s.name).filter(Boolean).slice(0, 10),
        rating: r.rating,
        reviews: r.reviews,
        projectsDone: completed.get(idStr(u)) || 0,
        avatarBg: bg,
        avatarFg: fg,
      };
    });

    return res.json({
      success: true,
      data: {
        demandSkills,
        recruitingProjects,
        skills: chips,
        candidates,
        totalCandidates: totalPublic,
        generatedAt: new Date().toISOString(),
      },
    });
  } catch (error) {
    console.error('[Match Demo Error]:', error);
    return res.status(500).json({ success: false, message: 'Failed to load match demo data' });
  }
};

module.exports = { getPlatformStats, getMatchDemo, publicName };
