const Review = require('../models/Review');
const f = require('../utils/format');
const { publicName } = require('./landingController');

/**
 * "Community Voice" — reviews of TeamUp itself, shown in the landing page's "Who it's for" section.
 *
 *  - Only logged-in users can write one (admins can't), and each user has exactly one:
 *    writing again replaces the text and sends it back for approval.
 *  - The author's name and "Who it's for" tab come from their account, never from the form.
 *  - New / edited reviews are saved as "Pending" and stay invisible until an admin approves them
 *    in Admin → Reviews & Feedback (status becomes "Published").
 */

const COMMUNITY_TYPE = 'Community Voice';
const MIN_LEN = 10;
const MAX_LEN = 500;

/** Account role → the "Who it's for" tab the review is shown under. */
const tabForRole = (role = '') => {
  const r = String(role).toLowerCase();
  if (r.includes('developer')) return 'Developers';
  if (r.includes('research')) return 'Researchers';
  if (r.includes('freelanc') || r.includes('designer')) return 'Freelancers';
  if (r.includes('leader') || r.includes('founder') || r.includes('startup')) return 'Startups';
  return 'Students';
};

/** Next free "REV-###" id (count-based ids collide after deletions). */
const nextReviewId = async () => {
  const ids = await Review.find({ reviewId: /^REV-\d+$/ }).select('reviewId').lean();
  let max = 500;
  for (const r of ids) {
    const n = parseInt(String(r.reviewId).slice(4), 10);
    if (n > max) max = n;
  }
  return `REV-${max + 1}`;
};

const toPublic = (r) => ({
  id: r.reviewId || String(r._id),
  author: publicName(r.reviewerName),
  initials: r.reviewerInitials || f.initials(r.reviewerName),
  tab: r.roleOrContext || 'Students',
  rating: r.rating || 5,
  text: r.reviewText || r.feedback || '',
  date: f.longDate(r.updatedAt || r.createdAt),
});

const toMine = (r) => ({
  id: r.reviewId || String(r._id),
  status: r.status,
  tab: r.roleOrContext,
  rating: r.rating,
  text: r.reviewText || '',
  date: f.longDate(r.updatedAt || r.createdAt),
});

/** GET /api/community-reviews — approved reviews only (public, no login). */
const listPublished = async (req, res) => {
  try {
    const rows = await Review.find({ reviewType: COMMUNITY_TYPE, status: 'Published' })
      .sort({ updatedAt: -1 })
      .limit(100)
      .lean();
    return res.json({ success: true, data: rows.map(toPublic) });
  } catch (error) {
    console.error('[Community Reviews Error]:', error);
    return res.status(500).json({ success: false, message: 'Failed to load community reviews' });
  }
};

/** GET /api/community-reviews/mine — the signed-in user's own review and its moderation status. */
const getMine = async (req, res) => {
  try {
    const mine = await Review.findOne({ reviewType: COMMUNITY_TYPE, reviewer: req.user._id }).lean();
    return res.json({
      success: true,
      data: { review: mine ? toMine(mine) : null, tab: tabForRole(req.user.role), canReview: req.user.role !== 'Admin' },
    });
  } catch (error) {
    console.error('[Community Reviews Error]:', error);
    return res.status(500).json({ success: false, message: 'Failed to load your review' });
  }
};

/** POST /api/community-reviews { text, rating } — create or update your review; it goes to "Pending". */
const submit = async (req, res) => {
  try {
    if (req.user.role === 'Admin') {
      return res.status(403).json({ success: false, message: 'Admins can’t post community reviews.' });
    }
    if (req.user.status === 'Suspended') {
      return res.status(403).json({ success: false, message: 'Suspended accounts can’t post reviews.' });
    }
    const text = String(req.body.text || '').replace(/\s+/g, ' ').trim();
    if (text.length < MIN_LEN) {
      return res.status(400).json({ success: false, message: `Please write at least ${MIN_LEN} characters.` });
    }
    if (text.length > MAX_LEN) {
      return res.status(400).json({ success: false, message: `Please keep it under ${MAX_LEN} characters.` });
    }
    const rating = Math.min(5, Math.max(1, parseInt(req.body.rating, 10) || 5));
    const tab = tabForRole(req.user.role);

    let review = await Review.findOne({ reviewType: COMMUNITY_TYPE, reviewer: req.user._id });
    const isNew = !review;
    if (isNew) {
      review = new Review({ reviewId: await nextReviewId(), reviewer: req.user._id, reviewType: COMMUNITY_TYPE });
    }
    review.reviewerName = req.user.name;
    review.reviewerInitials = f.initials(req.user.name);
    review.revieweeName = 'TeamUp';
    review.projectName = '';
    review.roleOrContext = tab;
    review.rating = rating;
    review.reviewText = text;
    review.feedback = text;
    review.date = f.longDate(new Date());
    review.status = 'Pending';
    review.reportReason = null;
    review.reportedBy = null;
    review.verifiedCollaboration = false;
    await review.save();

    return res.status(isNew ? 201 : 200).json({
      success: true,
      message: isNew
        ? 'Thanks! Your review was sent for approval and will appear once an admin approves it.'
        : 'Updated! Your review was sent for approval again.',
      data: toMine(review),
    });
  } catch (error) {
    console.error('[Community Reviews Error]:', error);
    return res.status(500).json({ success: false, message: 'Failed to save your review' });
  }
};

module.exports = { listPublished, getMine, submit, tabForRole, nextReviewId, COMMUNITY_TYPE };
