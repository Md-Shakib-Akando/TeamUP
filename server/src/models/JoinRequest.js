const mongoose = require('mongoose');

/** A student asking to join a project (shown to the project lead as a candidate). */
const joinRequestSchema = new mongoose.Schema(
  {
    project: { type: mongoose.Schema.Types.ObjectId, ref: 'Project', required: true, index: true },
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    role: { type: String, trim: true, default: 'Contributor' },
    matchPct: { type: Number, min: 0, max: 100, default: 0 },
    message: { type: String, trim: true, default: '' },
    status: { type: String, enum: ['pending', 'accepted', 'declined'], default: 'pending' },
    decidedAt: { type: Date },
  },
  { timestamps: true }
);

joinRequestSchema.index({ project: 1, user: 1 }, { unique: true });

module.exports = mongoose.model('JoinRequest', joinRequestSchema);
