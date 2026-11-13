const express = require('express');
const router = express.Router();
const { register, login, getMe, listUsers } = require('../controllers/authController');
const { protect, requireAdmin } = require('../middleware/auth');

router.post('/register', register);
router.post('/login', login);
router.get('/me', protect, getMe);
router.get('/users', protect, requireAdmin, listUsers);

module.exports = router;

