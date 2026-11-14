const mongoose = require('mongoose');

const reportSchema = new mongoose.Schema(
  {
    reportId: { type: String, index: true }, // e.g. "R-1048"
    type: {
      type: String,
      enum: ['Message', 'Project', 'Profile', 'Comment', 'Other'],
      default: 'Message',
    },
    title: { type: String, required: true, trim: true },
    category: {
      type: String,
      default: 'Spam',
      trim: true,
    },
    severity: {
      type: String,
      enum: ['High', 'Medium', 'Low'],
      default: 'Medium',
    },
    reporter: { type: String, default: '', trim: true },
    reporterInitials: { type: String, default: '', trim: true },
    date: { type: String, default: '' },
    timeUtc: { type: String, default: '' },
    status: {
      type: String,
      enum: ['Pending', 'In review', 'Resolved'],
      default: 'Pending',
    },
    reporterDesc: { type: String, default: '', trim: true },
    flaggedAuthor: { type: String, default: '', trim: true },
    flaggedContent: { type: String, default: '', trim: true },
    flaggedTime: { type: String, default: '' },
    internalNote: { type: String, default: '', trim: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Report', reportSchema);

