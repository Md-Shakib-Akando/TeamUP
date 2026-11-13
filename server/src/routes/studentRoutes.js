const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/auth');
const { getDashboard } = require('../controllers/dashboardController');
const s = require('../controllers/studentController');

// Every student dashboard endpoint requires a valid JWT (Authorization: Bearer <token>)
router.get('/dashboard', protect, getDashboard);

router.post('/projects', protect, s.createProject);
router.post('/projects/:id/bookmark', protect, s.toggleBookmark);
router.post('/projects/:id/join', protect, s.requestJoin);
router.post('/projects/:id/tasks', protect, s.addTask);

router.post('/tasks/:id/toggle', protect, s.toggleTask);

router.post('/requests/:id/accept', protect, s.acceptRequest);
router.post('/requests/:id/decline', protect, s.declineRequest);

router.post('/users/:id/invite', protect, s.inviteUser);
router.post('/users/:id/collab', protect, s.collabUser);

router.put('/me/profile', protect, s.updateProfile);
router.put('/me/settings', protect, s.updateSettings);
router.post('/me/skills/:name/endorse', protect, s.toggleEndorsement);

router.post('/notifications/read', protect, s.markNotificationsRead);

module.exports = router;
