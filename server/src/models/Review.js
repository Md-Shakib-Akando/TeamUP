const mongoose = require('mongoose');

/** Peer review left by one teammate about another after working together. */
const reviewSchema = new mongoose.Schema(
  {
    reviewer: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    reviewee: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    project: { type: mongoose.Schema.Types.ObjectId, ref: 'Project' },
    rating: { type: Number, min: 1, max: 5, required: true },
    feedback: { type: String, trim: true, default: '' },
  },
  { timestamps: true }
);

reviewSchema.index({ reviewer: 1, reviewee: 1, project: 1 }, { unique: true });

module.exports = mongoose.model('Review', reviewSchema);
