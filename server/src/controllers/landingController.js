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
      Review.find({ reviewType: { $ne: 'Community Voice' } }).select('rating').lean(),
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

const CANDIDATE_FIELDS = 'name headline university availability skills bio avatarBg avatarFg ratingBase reviewsBase';

/** Public card for one profile (shortened name, no contact details). */
const toCandidate = (u, ratingAgg, completed) => {
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
};

/** Ratings (peer reviews only) and completed-project counts, keyed by user id. */
const loadRatingsAndCompleted = async () => {
  const [reviewAgg, completedAgg] = await Promise.all([
    Review.aggregate([
      { $match: { reviewee: { $ne: null }, reviewType: { $ne: 'Community Voice' } } },
      { $group: { _id: '$reviewee', sum: { $sum: '$rating' }, count: { $sum: 1 } } },
    ]),
    Project.aggregate([
      { $match: { status: 'Completed' } },
      { $unwind: '$members' },
      { $group: { _id: '$members.user', n: { $sum: 1 } } },
    ]),
  ]);
  return {
    ratingAgg: new Map(reviewAgg.map((r) => [String(r._id), r])),
    completed: new Map(completedAgg.map((r) => [String(r._id), r.n])),
  };
};

const escapeRegex = (s) => String(s).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
/** Whole-word match ("java" must not match inside "javascript"; works for "c++" too). */
const wordRegex = (w) => new RegExp(`(?:^|[^a-z0-9])${escapeRegex(w)}(?![a-z0-9])`, 'i');
const norm = (s) => String(s || '').toLowerCase().replace(/[^a-z0-9+#.]+/g, ' ').trim();

/** Everyday words people type that point at skills ("backend", "ml", "database" ...). */
const SYNONYMS = {
  frontend: ['react', 'javascript', 'typescript', 'html', 'css', 'vue', 'angular', 'next.js'],
  'front end': ['react', 'javascript', 'html', 'css'],
  backend: ['node.js', 'express', 'java', 'spring', 'python', 'django', 'mysql', 'mongodb'],
  'back end': ['node.js', 'java', 'python'],
  fullstack: ['react', 'node.js', 'javascript', 'mongodb'],
  'full stack': ['react', 'node.js', 'javascript', 'mongodb'],
  database: ['mysql', 'mongodb', 'postgresql', 'sql'],
  db: ['mysql', 'mongodb', 'postgresql', 'sql'],
  design: ['ui/ux', 'ui/ux design', 'figma', 'product design'],
  designer: ['ui/ux', 'ui/ux design', 'figma'],
  ux: ['ui/ux', 'ui/ux design', 'figma'],
  ui: ['ui/ux', 'ui/ux design', 'figma'],
  ml: ['machine learning', 'python', 'tensorflow', 'pytorch'],
  ai: ['machine learning', 'python', 'tensorflow', 'pytorch'],
  data: ['data science', 'python', 'sql', 'data analysis'],
  mobile: ['flutter', 'react native', 'kotlin', 'swift', 'android'],
  cloud: ['aws', 'docker', 'kubernetes', 'devops'],
  devops: ['docker', 'kubernetes', 'aws', 'ci/cd'],
  testing: ['qa', 'testing', 'selenium', 'jest'],
  qa: ['qa', 'testing'],
};

/**
 * Which known skills does a free-text query mean? A skill matches when its full name appears in
 * the query ("react native dev" -> React Native, React), when a query word starts the skill name
 * ("pyth" -> Python), or through SYNONYMS ("backend" -> Node.js, Java ...). Java never matches JavaScript.
 */
const skillsForQuery = (q, knownSkills) => {
  const nq = ` ${norm(q)} `;
  const words = norm(q).split(' ').filter((w) => w.length >= 2);
  const hits = new Map(); // lower name -> score
  const add = (key, score) => hits.set(key, Math.max(hits.get(key) || 0, score));
  for (const [key] of knownSkills) {
    const nk = norm(key);
    if (!nk) continue;
    if (nq.includes(` ${nk} `)) add(key, 3);
    else if (words.some((w) => w.length >= 3 && nk.split(' ').some((part) => part.startsWith(w)) && !(w === 'java' && nk.startsWith('javascript')))) add(key, 2);
  }
  for (const [word, targets] of Object.entries(SYNONYMS)) {
    if (!nq.includes(` ${word} `)) continue;
    for (const t of targets) if (knownSkills.has(t)) add(t, 1);
  }
  // "react native" names React Native — don't also count the shorter skill inside it (React)
  const exact = [...hits.entries()].filter(([, sc]) => sc === 3).map(([k]) => norm(k));
  for (const [key] of [...hits.entries()]) {
    const nk = norm(key);
    if (exact.some((e) => e !== nk && ` ${e} `.includes(` ${nk} `))) hits.delete(key);
  }
  return [...hits.entries()].sort((a, b) => b[1] - a[1] || b[0].length - a[0].length).map(([k]) => knownSkills.get(k));
};

/**
 * GET /api/match-demo/search?q=react native — hero search on the landing page.
 * Searches every public profile in the database by skill, headline and bio.
 * Returns the skills the query was understood as, plus the matching profiles (best first).
 */
const searchMatchDemo = async (req, res) => {
  try {
    const q = String(req.query.q || '').trim().slice(0, 80);
    if (!q) return res.status(400).json({ success: false, message: 'Type a skill to search for.' });

    const base = { ...PUBLIC_PROFILE, 'skills.0': { $exists: true } };
    const skillNames = await User.distinct('skills.name', base);
    const known = new Map();
    for (const n of skillNames) {
      const key = String(n || '').trim().toLowerCase();
      if (key && !known.has(key)) known.set(key, String(n).trim());
    }
    const skills = skillsForQuery(q, known).slice(0, 6);

    const words = norm(q).split(' ').filter((w) => w.length >= 3);
    const or = [];
    if (skills.length) or.push({ 'skills.name': { $in: skills.map((s) => new RegExp(`^${escapeRegex(s)}$`, 'i')) } });
    for (const w of words.slice(0, 5)) {
      const rx = wordRegex(w);
      or.push({ headline: rx }, { bio: rx });
    }
    if (!or.length) {
      return res.json({ success: true, data: { query: q, skills: [], candidates: [], total: 0 } });
    }

    const [users, { ratingAgg, completed }] = await Promise.all([
      User.find({ ...base, $or: or }).sort({ updatedAt: -1 }).limit(80).select(CANDIDATE_FIELDS).lean(),
      loadRatingsAndCompleted(),
    ]);

    // Rank: the understood skills they have (the first, most specific one counts most),
    // then whole-word hits in headline/bio, then peer rating
    const wanted = skills.map((s) => s.toLowerCase());
    const wordRx = words.map(wordRegex);
    const scored = users.map((u) => {
      const have = new Set((u.skills || []).map((s) => String(s.name || '').toLowerCase()));
      const skillScore = wanted.reduce((sum, w, i) => sum + (have.has(w) ? (i === 0 ? 12 : 6) : 0), 0);
      const text = `${u.headline || ''} ${u.bio || ''}`;
      const textHits = wordRx.filter((rx) => rx.test(text)).length;
      const c = toCandidate(u, ratingAgg, completed);
      return { c, score: skillScore + textHits * 2 + (c.rating || 0) / 5 };
    });
    scored.sort((a, b) => b.score - a.score);

    return res.json({
      success: true,
      data: { query: q, skills, candidates: scored.slice(0, 40).map((x) => x.c), total: scored.length },
    });
  } catch (error) {
    console.error('[Match Search Error]:', error);
    return res.status(500).json({ success: false, message: 'Search failed' });
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

    const candidates = users.map((u) => toCandidate(u, ratingAgg, completed));

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

module.exports = { getPlatformStats, getMatchDemo, searchMatchDemo, publicName, skillsForQuery };
