const mongoose = require('mongoose');

/** Something that happened inside a project (shown in Recent activity / team feed). */
const activitySchema = new mongoose.Schema(
  {
    project: { type: mongoose.Schema.Types.ObjectId, ref: 'Project', index: true },
    actor: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    type: {
      type: String,
      enum: ['design', 'completed', 'task', 'joined', 'created', 'file', 'review'],
      default: 'task',
    },
    text: { type: String, required: true, trim: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Activity', activitySchema);
