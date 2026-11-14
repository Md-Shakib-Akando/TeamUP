const mongoose = require('mongoose');

const taskSchema = new mongoose.Schema(
  {
    project: { type: mongoose.Schema.Types.ObjectId, ref: 'Project', required: true, index: true },
    title: { type: String, required: true, trim: true, maxlength: 140 },
    status: { type: String, enum: ['todo', 'in_progress', 'in_review', 'done'], default: 'todo' },
    dueDate: { type: Date },
    assignee: { type: mongoose.Schema.Types.ObjectId, ref: 'User', index: true },
    createdBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    completedAt: { type: Date },
  },
  { timestamps: true }
);

taskSchema.virtual('done').get(function () {
  return this.status === 'done';
});

module.exports = mongoose.model('Task', taskSchema);
