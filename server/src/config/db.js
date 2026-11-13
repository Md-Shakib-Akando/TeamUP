const mongoose = require('mongoose');

/**
 * TeamUp keeps its data in its own database (default "teamup") so it never mixes
 * with other apps that share the same Atlas cluster. Override with MONGODB_DB_NAME.
 */
const connectDB = async () => {
  try {
    const connUri = process.env.MONGODB_URI;
    if (!connUri) {
      console.warn('[MongoDB] WARNING: MONGODB_URI not set in environment or .env file.');
      console.warn('[MongoDB] Please provide your MongoDB Atlas connection string in server/.env');
      return null;
    }

    const conn = await mongoose.connect(connUri, {
      dbName: process.env.MONGODB_DB_NAME || 'teamup',
      serverSelectionTimeoutMS: 8000,
    });

    console.log(`[MongoDB Atlas] Connected: ${conn.connection.host} / db=${conn.connection.name}`);
    return conn;
  } catch (error) {
    console.error(`[MongoDB Atlas] Connection error: ${error.message}`);
    // Don't crash server immediately, allowing health check and helpful error response
    return null;
  }
};

module.exports = connectDB;
