const mongoose = require('mongoose');

const memberSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    role: { type: String, trim: true, default: 'Member' },
    joinedAt: { type: Date, default: Date.now },
  },
  { _id: false }
);

const milestoneSchema = new mongoose.Schema({
  title: { type: String, required: true, trim: true },
  dueDate: { type: Date },
  status: { type: String, enum: ['Completed', 'In progress', 'Upcoming'], default: 'Upcoming' },
  progress: { type: Number, min: 0, max: 100, default: 0 },
});

const fileSchema = new mongoose.Schema({
  name: { type: String, required: true, trim: true },
  type: { type: String, default: 'File' },
  size: { type: String, default: '' },
  authorName: { type: String, default: '' },
  updatedAt: { type: Date, default: Date.now },
});

const projectSchema = new mongoose.Schema(
  {
    title: { type: String, required: true, trim: true, maxlength: 80 },
    category: { type: String, default: 'Campus life', trim: true },
    experience: { type: String, enum: ['Beginner', 'Intermediate', 'Advanced'], default: 'Intermediate' },
    description: { type: String, default: '', trim: true, maxlength: 600 },
    skills: { type: [String], default: [] },
    status: { type: String, enum: ['Recruiting', 'Active', 'Completed'], default: 'Recruiting' },
    archived: { type: Boolean, default: false },
    maxMembers: { type: Number, min: 1, max: 50, default: 4 },
    openRoles: { type: [String], default: [] },
    iconType: { type: Number, min: 0, max: 6, default: 0 },
    iconBg: { type: String, default: '#FDEEF4' },
    iconColor: { type: String, default: '#EA4C89' },
    nextMilestone: { type: String, default: '' },
    owner: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    members: { type: [memberSchema], default: [] },
    milestones: { type: [milestoneSchema], default: [] },
    files: { type: [fileSchema], default: [] },
    nextSyncAt: { type: Date },
    lastSyncAt: { type: Date },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

projectSchema.index({ 'members.user': 1 });
projectSchema.index({ title: 1 });

projectSchema.methods.isMember = function (userId) {
  return this.members.some((m) => String(m.user._id || m.user) === String(userId));
};

module.exports = mongoose.model('Project', projectSchema);
