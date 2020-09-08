var express = require('express');
var router = express.Router();

var mysql = require('mysql');
var dbconfig = require('../config/dbconfig.js');
var connection = mysql.createConnection(dbconfig);




// user login
router.post('/login', function(req, res) {
    const { name, phoneNum } = req.body;
    const sql = "SELECT * FROM user WHERE name = '" + name + "' AND phone_num = '" + phoneNum + "'";
    
    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:'error'});
            return;
        }
        if(rows[0]){
            res.send({result:true,msg:'success'});
        }
        else{
            res.send({result:false,msg:'fail'});
        }
    });
});

// exist check phoneNum
router.post('/phoneNum', function(req, res){
    const { phoneNum } = req.body;

    phoneNumCheck(phoneNum)
        .then(function(result){
            if(result){
                res.send({result:false,msg:'exist'});
            }else{
                res.send({result:true,msg:'possible'});
            }
        });
    });

// user join
router.post('/join', function(req, res){
    const { name, phoneNum } = req.body;
    var sql = "SELECT * FROM user WHERE phone_num = '" + phoneNum + "'";
    phoneNumCheck(phoneNum)
        .then(function(result) {
            if(!result){
                sql = "INSERT INTO user(name,phone_num) VALUES(?, ?)";
                var params = [name, phoneNum];
                connection.query(sql, params, function(err, rows, fields){
                    if(err){
                        res.send({result:false,msg:'error'});
                    }else{
                        res.send({result:true,msg:'success'});
                    }
                });
            }else{
                res.send({result:false,msg:'fail'});
            }
        })
        .catch(function(err) {
            res.send({result:false,msg:'fail'});
            console.log(err);
        });
});

// exist check phoneNum
function phoneNumCheck(phoneNum) {
    return new Promise(resolve=>{
        const sql = "SELECT * FROM user WHERE phone_num = '" + phoneNum + "'";
        connection.query(sql,function(err, rows){
            if(err) return false;
    
            if(rows[0]){ // exist email
                resolve(true);
            }else {
                resolve(false);
            }
        });
    })
}


module.exports = router;
