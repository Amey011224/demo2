// bad-example.js
const express = require('express');
const mysql = require('mysql');
const app = express();

app.get('/user', function (req, res) {
    const connection = mysql.createConnection({
        host: 'localhost',
        user: 'root',
        password: '',
        database: 'testdb'
    });

    const query = "SELECT * FROM users WHERE username = '" + req.query.username + "'";
    connection.query(query, function (err, results) {
        if (err) throw err;
        res.json(results);
    });
});

app.listen(3000);
