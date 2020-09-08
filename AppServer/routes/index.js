var express = require('express');
var router = express.Router();

var mysql = require('mysql');
var dbconfig = require('../config/dbconfig.js');
var connection = mysql.createConnection(dbconfig);

router.post('/query', function(req, res){
    var { sql } = req.body;
    console.log(req);
    
    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
        res.send({result:true, data:rows, msg:'success'});
        
    });
});

// data set
router.post('/data', function(req, res) {
    var { data } = req.body;
    
    if(data == null) return;
    var sql = "INSERT INTO data_usage(phone_num, category, app, data_date, hour, mobile, wifi, use_time) values";
    var app_sql = "INSERT IGNORE INTO app(name,category) values";
    data.forEach(element=>{
        var { phoneNum, category, app, dataDate, hour, mobile, wifi, useTime} = element;
        sql = sql.concat(" ","('",phoneNum,"','",category,"','",app,"','",dataDate,"',",hour,",",mobile,",",wifi,",",useTime,"),");
        app_sql = app_sql.concat(" ","('",app,"','",category,"'),");
    });
    var rows_temp;
    connection.query(sql.slice(0,-1), function(err,rows, field){
        if(err){
            console.log(err);
            res.send({result:false,msg:err});
            return;
        }
        res.send({result:true,msg:'success'});
        rows_temp = rows;
    });
    insert_app_table_data(app_sql.slice(0,-1));
});

// data day get
router.get('/data/day', function(req, res) {
    const { phoneNum, dataDate } = req.query;

    // const sql = "SELECT date_format(data_date,'%Y-%m-%d') as data_date, sum(mobile) as mobile, sum(wifi) as wifi, sum(use_time) as use_time " +
    //             "FROM data_usage WHERE phone_num = '" + phoneNum + "' AND data_date = '"+dataDate+"' AND hour = " +
    //             "(SELECT max(hour) FROM data_usage WHERE phone_num = '"+phoneNum +"' AND data_date = '" + dataDate + "') group by data_date";
    const sql = "SELECT date_format(data_date,'%Y-%m-%d') as data_date, sum(mobile) as mobile, sum(wifi) as wifi, sum(use_time) as use_time " +
                "FROM data_usage WHERE phone_num = '" + phoneNum + "' AND data_date = '"+dataDate+"'" +
                " group by data_date";
    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
    
        if(rows[0]){
            res.send({result:true,data:rows[0],msg:'success'});
        }
        else{
            res.send({result:false,msg:'fail'});
        }
    });
});

// data month get
router.get('/data/month', function(req, res) {
    const { phoneNum, dataDate } = req.query;
    var date_date_split = dataDate.split('-');
    // const sql = "SELECT MONTH(data_date) as data_date, sum(mobile) as mobile, sum(wifi) as wifi, sum(use_time) as use_time " +
    //             "FROM (SELECT phone_num, mobile,wifi,use_time, data_date, max(hour) FROM data_usage as DU GROUP BY phone_num, data_date, mobile, wifi, use_time HAVING max(hour)=(select max(hour) from data_usage where DU.data_date = data_date)) as data_usage " +
    //             "WHERE phone_num = '" + phoneNum + "' AND YEAR(data_date) = "+date_date_split[0]+" AND MONTH(data_date) = " + date_date_split[1] + " group by MONTH(data_date)";
    const sql = "SELECT MONTH(data_date) as data_date, sum(mobile) as mobile, sum(wifi) as wifi, sum(use_time) as use_time " +
                "FROM data_usage " +
                "WHERE phone_num = '" + phoneNum + "' AND YEAR(data_date) = "+date_date_split[0]+" AND MONTH(data_date) = " + date_date_split[1] + " group by MONTH(data_date)";
    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
    
        if(rows[0]){
            res.send({result:true,data:rows[0],msg:'success'});
        }
        else{
            res.send({result:false,msg:'fail'});
        }
    });
});

// data week get
router.get('/data/week', function(req, res) {
    const { phoneNum, startDate, endDate } = req.query;

    const sql = "SELECT mobile, wifi, use_time " +
                "FROM data_usage " +
                "WHERE phone_num = '" + phoneNum + "' AND '"+startDate+"' <= data_date AND data_date <= '"+endDate+"'";

    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
        if(rows[0]){
            var mobile = 0;
            var wifi = 0;
            var use_time = 0;
            for(var i=0; i < rows.length; ++i){
                mobile += rows[i].mobile;
                wifi += rows[i].wifi;
                use_time += rows[i].use_time;
            }
            res.send({result:true,data:{"mobile":mobile,"wifi":wifi,"use_time":use_time},msg:'success'});
        }
        else{
            res.send({result:false,msg:'fail'});
        }
    });
});

// screen set
router.post('/screen', function(req, res) {
    const { phoneNum, count, screenDate, hour} = req.body;
    var sql = "INSERT INTO screen(phone_num, count, screen_date, hour) values(?,?,?,?)";
    var params = [phoneNum, count, screenDate, hour];
    connection.query(sql, params, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
        res.send({result:true,msg:'success'});
    });
});

// screen get
router.get('/screen/day', function(req, res) {
    const { phoneNum, screenDate } = req.query;

    const sql = "SELECT phone_num, count, date_format(screen_date,'%Y-%m-%d') as screen_date, hour FROM screen WHERE phone_num = '" + phoneNum + "' AND screen_date = '"+ screenDate +"' order by hour desc";
    
    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
    
        if(rows[0]){
            res.send({result:true,data:rows[0],msg:'success'});
        }
        else{
            res.send({result:false,msg:'fail'});
        }
    });
});

// screen month get
router.get('/screen/month', function(req, res) {
    const { phoneNum, screenDate } = req.query;
    var screen_date_split = screenDate.split('-');
    // const sql = "SELECT SUM(count) as count, MONTH(screen_date) as screen_date FROM (SELECT phone_num, count, screen_date, max(hour) FROM screen as S GROUP BY phone_num,screen_date, count HAVING max(hour)=(select max(hour) from screen where S.screen_date = screen_date)) as screen WHERE phone_num = '" + phoneNum + "' AND YEAR(screen_date) = "+ screen_date_split[0] +" AND MONTH(screen_date) = "+ screen_date_split[1] +" GROUP BY MONTH(screen_date)";
    const sql = "SELECT SUM(count) as count, MONTH(screen_date) as screen_date FROM screen WHERE phone_num = '" + phoneNum + "' AND YEAR(screen_date) = "+ screen_date_split[0] +" AND MONTH(screen_date) = "+ screen_date_split[1] +" GROUP BY MONTH(screen_date)";
    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
    
        if(rows[0]){
            res.send({result:true,data:rows[0],msg:'success'});
        }
        else{
            res.send({result:false,msg:'fail'});
        }
    });
});

// screen week get
router.get('/screen/week', function(req, res) {
    const { phoneNum, startDate, endDate } = req.query;

    // const sql = "SELECT SUM(count) as count FROM (SELECT phone_num, count, screen_date, max(hour) FROM screen as S GROUP BY phone_num,screen_date, count HAVING max(hour)=(select max(hour) from screen where S.screen_date = screen_date)) as screen WHERE phone_num = '" + phoneNum + "' AND '" + startDate + "' <=screen_date AND screen_date <= '" + endDate + "'";
    const sql = "SELECT SUM(count) as count FROM screen WHERE phone_num = '" + phoneNum + "' AND '" + startDate + "' <=screen_date AND screen_date <= '" + endDate + "'";
    connection.query(sql, function(err,rows, field){
        if(err){
            res.send({result:false,msg:err});
            return;
        }
    
        if(rows[0]){
            res.send({result:true,data:rows[0].count == null ? {'count':0}:rows[0],msg:'success'});
        }
        else{
            res.send({result:false,msg:'fail'});
        }
    });
});

function insert_app_table_data(sql){
    connection.query(sql,function(err,rows, field){
        if(err){
            console.log(err);
            return;
        }
    });
}
module.exports = router;