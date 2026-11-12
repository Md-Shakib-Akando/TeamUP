const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const connUri = process.env.MONGODB_URI;
    if (!connUri) {
      console.warn('[MongoDB] WARNING: MONGODB_URI not set in environment or .env file.');
      console.warn('[MongoDB] Please provide your MongoDB Atlas connection string in server/.env');
      return;
    }

    const conn = await mongoose.connect(connUri, {
      serverSelectionTimeoutMS: 5000,
    });

    console.log(`[MongoDB Atlas] Connected successfully: ${conn.connection.host}`);
  } catch (error) {
    console.error(`[MongoDB Atlas] Connection error: ${error.message}`);
    // Don't crash server immediately, allowing health check and helpful error response
  }
};

module.exports = connectDB;

