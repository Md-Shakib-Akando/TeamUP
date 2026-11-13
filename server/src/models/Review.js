const mongoose = require('mongoose');

/** Peer review & feedback left about teammates, projects, or platform collaborations. */
const reviewSchema = new mongoose.Schema(
  {
    reviewId: { type: String, index: true }, // e.g. "REV-501"
    reviewer: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    reviewee: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    reviewerName: { type: String, trim: true, default: '' },
    reviewerInitials: { type: String, trim: true, default: '' },
    revieweeName: { type: String, trim: true, default: '' },
    project: { type: mongoose.Schema.Types.ObjectId, ref: 'Project' },
    projectName: { type: String, trim: true, default: '' },
    roleOrContext: { type: String, trim: true, default: '' },
    rating: { type: Number, min: 1, max: 5, required: true },
    feedback: { type: String, trim: true, default: '' },
    reviewText: { type: String, trim: true, default: '' },
    reviewType: { type: String, default: 'Team Member Review' },
    date: { type: String, default: '' },
    status: {
      type: String,
      enum: ['Published', 'Reported', 'Under review', 'Hidden'],
      default: 'Published',
    },
    reportReason: { type: String, default: null },
    reportedBy: { type: String, default: null },
    helpfulVotes: { type: Number, default: 0 },
    verifiedCollaboration: { type: Boolean, default: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Review', reviewSchema);
