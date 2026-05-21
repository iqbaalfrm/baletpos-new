const { Pool } = require("pg");

let pool;

function getConnectionString() {
  return process.env.DATABASE_URL
    || process.env.POSTGRES_URL
    || process.env.BALETPOSTGRES_URL;
}

function getPool() {
  const connectionString = getConnectionString();
  if (!connectionString) {
    throw new Error("DATABASE_URL/POSTGRES_URL belum diset di Vercel.");
  }

  if (!pool) {
    pool = new Pool({
      connectionString,
      ssl: connectionString.includes("localhost") ? false : { rejectUnauthorized: false },
      max: 3,
    });
  }

  return pool;
}

async function query(text, params = []) {
  const result = await getPool().query(text, params);
  return result.rows;
}

function sendJson(res, status, body) {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.end(JSON.stringify(body));
}

function handleError(res, error) {
  console.error(error);
  sendJson(res, 500, {
    error: "Database error",
    message: error.message,
  });
}

function getDateRange(period) {
  const now = new Date();
  const end = new Date(now);
  end.setHours(23, 59, 59, 999);

  const start = new Date(now);
  start.setHours(0, 0, 0, 0);

  if (period === "week") {
    const day = start.getDay() || 7;
    start.setDate(start.getDate() - day + 1);
  } else if (period === "month") {
    start.setDate(1);
  }

  return {
    start: start.toISOString(),
    end: end.toISOString(),
  };
}

module.exports = {
  query,
  sendJson,
  handleError,
  getDateRange,
};
