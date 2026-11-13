const mongoose = require('mongoose');

/**
 * Person-to-person outreach from Find Teammates:
 *  - kind "invite": invite someone to collaborate (optionally on one of your projects)
 *  - kind "collab": request to collaborate with someone
 */
const invitationSchema = new mongoose.Schema(
  {
    from: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    to: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    kind: { type: String, enum: ['invite', 'collab'], default: 'invite' },
    project: { type: mongoose.Schema.Types.ObjectId, ref: 'Project' },
    status: { type: String, enum: ['pending', 'accepted', 'declined'], default: 'pending' },
  },
  { timestamps: true }
);

invitationSchema.index({ from: 1, to: 1, kind: 1 }, { unique: true });

module.exports = mongoose.model('Invitation', invitationSchema);
