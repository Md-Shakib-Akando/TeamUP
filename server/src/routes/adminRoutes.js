const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');

// Bootstrap full admin dashboard data (Users, Projects, Reports, Categories, Skills, Reviews, Logs, Settings)
router.get('/bootstrap', adminController.getAdminBootstrap);

// Users management
router.post('/users', adminController.createUser);
router.put('/users/:id/role', adminController.updateUserRole);
router.put('/users/:id/status', adminController.updateUserStatus);
router.delete('/users/:id', adminController.deleteUser);
router.delete('/users', adminController.deleteUser);

// Projects management
router.post('/projects', adminController.createProject);
router.put('/projects/:id/status', adminController.updateProjectStatus);
router.delete('/projects/:id', adminController.deleteProject);
router.delete('/projects', adminController.deleteProject);

// Reports & moderation
router.put('/reports/:id/resolve', adminController.resolveReport);
router.put('/reports/:id/action', adminController.actionReport);

// Categories & Skills
router.post('/categories', adminController.createCategory);
router.put('/categories/:id', adminController.updateCategory);
router.put('/categories', adminController.updateCategory);
router.post('/skills', adminController.createSkill);
router.put('/skills/:id', adminController.updateSkill);
router.put('/skills', adminController.updateSkill);

// Reviews & feedback
router.post('/reviews', adminController.createReview);
router.put('/reviews/:id', adminController.updateReview);
router.delete('/reviews/:id', adminController.deleteReview);

// Platform settings
router.put('/settings', adminController.updateSettings);
router.post('/settings/domains', adminController.addDomain);
router.delete('/settings/domains', adminController.removeDomain);
router.put('/settings/password', adminController.changeAdminPassword);
router.post('/settings/reset', adminController.resetSettings);

module.exports = router;

