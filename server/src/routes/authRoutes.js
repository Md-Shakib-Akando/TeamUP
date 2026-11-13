const express = require('express');
const router = express.Router();
const { register, login, getMe, listUsers, changePassword } = require('../controllers/authController');
const { protect, requireAdmin } = require('../middleware/auth');

router.post('/register', register);
router.post('/login', login);
router.get('/me', protect, getMe);
router.post('/change-password', protect, changePassword);
router.get('/users', protect, requireAdmin, listUsers);

module.exports = router;

