const mongoose = require('mongoose');

const auditLogSchema = new mongoose.Schema(
  {
    logId: { type: String, index: true }, // e.g. "L-1"
    actorName: { type: String, default: 'Jordan Lee', trim: true },
    actorInitials: { type: String, default: 'JL', trim: true },
    action: { type: String, required: true, trim: true },
    target: { type: String, default: '', trim: true },
    timestampLine1: { type: String, default: '' },
    timestampLine2: { type: String, default: '' },
    success: { type: Boolean, default: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('AuditLog', auditLogSchema);

