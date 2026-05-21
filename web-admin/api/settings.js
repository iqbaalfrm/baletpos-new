const { query, sendJson, handleError } = require("./_db");

module.exports = async function handler(req, res) {
  try {
    const rows = await query(
      `SELECT setting_key AS key, setting_value AS value, description
       FROM settings
       ORDER BY setting_key ASC`,
      []
    );
    return sendJson(res, 200, { settings: rows });
  } catch (error) {
    return handleError(res, error);
  }
};
