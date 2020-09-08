using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Forms.DataVisualization.Charting;
using System.Net;
using System.IO;
using Newtonsoft.Json.Linq;
using System.Data;

namespace how_much_do_you_spend
{
    /// <summary>
    /// MainWindow.xaml에 대한 상호 작용 논리
    /// </summary>



    public partial class MainWindow : Window
    {
        Chart chart = new System.Windows.Forms.DataVisualization.Charting.Chart();
        string url = "http://---.---.---.---:----/query";
        DataTable dataTable;

        enum State
        {
            month,
            week,
            date
        };
        State state = State.date;

        public MainWindow()
        {
            InitializeComponent();
            chart.Visible = true;
            dataTable = new DataTable();
            dataGrid.Visibility = Visibility.Collapsed;

            //request(url, "SELECT name FROM user");

            fillCombobox(category, "SELECT distinct category FROM app", "category");
            fillCombobox(application, "SELECT distinct name FROM app", "name");
            fillCombobox(people, "SELECT name, phone_num FROM user", "name", "phone_num");


            start.SelectedDate = DateTime.Now;
            end.SelectedDate = DateTime.Now;

            //makeChart(new List<string>());
            //Console.WriteLine(makeSQL());

        }

        private string makeSQL(string selected, string group)
        {

            string dataType = dataTypeSwitch_Btn.Content.Equals("Wifi") ? "sum(wifi/1024/1024) AS data," : ((dataTypeSwitch_Btn.Content.Equals("Mobile")) ? "sum(mobile/1024/1024) AS data," : ((dataTypeSwitch_Btn.Content.Equals("Mobile + WiFi")) ? "sum((mobile+wifi)/1024/1024) AS data," : "sum(use_time/60) AS data,"));
            string groupBy = " GROUP BY time " + group + " ORDER BY time";

            string intervalType = "";
            if (intervalSwitch_Btn.Content.Equals("기간별"))
            {
                intervalType = dateTypeSwitch_btn.Content.Equals("일별") ? "DATE_FORMAT(data_date, '%Y-%m-%d') AS time" : (dateTypeSwitch_btn.Content.Equals("주별") ? "DATE_FORMAT(data_date,'%Y-%U') AS time" : "MONTH(data_date) AS time");
            }
            else
            {
                intervalType = "hour AS time";

            }

            string[] peoSplit = people.Text.Split(' ');
            string categorySql = category.Text.Equals("모두 보기") ? "" : "category = '" + category.Text + "' AND ";
            string appNameSql = application.Text == "모두 보기" ? "" : "app = '" + application.Text + "' AND ";
            string peopleSql = peoSplit[0] == "모두" ? "" : "phone_num = '" + peoSplit[1].Substring(1, 11) + "' AND ";

            string intervalSql = "'" + start.SelectedDate.Value.ToString("yyyy-MM-dd") + "'<= data_date AND data_date <='" + end.SelectedDate.Value.ToString("yyyy-MM-dd") + "'";

            string selectSQL = "SELECT " + selected + " " + dataType + " " + intervalType + " FROM user_data_usage WHERE " + categorySql + appNameSql + peopleSql + intervalSql + groupBy;

            return selectSQL;
        }

        private void setScreenCount()
        {

            string[] peoSplit = people.Text.Split(' ');
            string peopleSql = peoSplit[0] == "모두" ? "" : "phone_num = '" + peoSplit[1].Substring(1, 11) + "' AND ";
            string intervalSql = "'" + start.SelectedDate.Value.ToString("yyyy-MM-dd") + "'<= date AND date <='" + end.SelectedDate.Value.ToString("yyyy-MM-dd") + "'";

            string result = request(url, "SELECT avg(count) AS count FROM user_screen WHERE " + peopleSql + intervalSql);


            JObject jsonObj = JObject.Parse(result);
            Console.WriteLine(jsonObj.ToString());
            JArray jsonArray = JArray.Parse(jsonObj.GetValue("data").ToString());




            foreach (JObject item in jsonArray)
            {
                screenCount.Text = (item.GetValue("count").ToString() == "" ? "0" : item.GetValue("count").ToString());
            }


        }

        private string request(string url, string query)
        {
            string returnStr = "";
            try
            {
                var httpWebRequest = (HttpWebRequest)WebRequest.Create(url);
                Console.WriteLine(query);
                httpWebRequest.ContentType = "application/json";
                httpWebRequest.Method = "POST";
                using (var streamWriter = new StreamWriter(httpWebRequest.GetRequestStream()))
                {
                    string json = "{\"sql\":\" " + query + "\"}";
                    streamWriter.Write(json);
                    streamWriter.Flush();
                    streamWriter.Close();
                }

                var httpResponse = (HttpWebResponse)httpWebRequest.GetResponse();
                using (var streamReader = new StreamReader(httpResponse.GetResponseStream()))
                {
                    returnStr = streamReader.ReadToEnd();
                    Console.WriteLine(returnStr.ToString());
                }
            }
            catch (Exception)
            {
                MessageBox.Show("서버와의 통신이 원활하지 않습니다.");
            }

            return returnStr;
        }


        private void fillCombobox(ComboBox comboBox, string sql, string name)
        {
            comboBox.Items.Clear();
            comboBox.Items.Add("모두 보기");

            string result = request(url, sql);

            JObject jsonObj = JObject.Parse(result);
            Console.WriteLine(jsonObj.ToString());
            JArray jsonArray = JArray.Parse(jsonObj.GetValue("data").ToString());

            foreach (JObject item in jsonArray)
            {
                comboBox.Items.Add(item.GetValue(name).ToString());
            }

            comboBox.SelectedIndex = 0;
        }

        private void fillCombobox(ComboBox comboBox, string sql, string name, string phoneNum)
        {
            comboBox.Items.Clear();
            comboBox.Items.Add("모두 보기");

            string result = request(url, sql);


            JObject jsonObj = JObject.Parse(result);
            Console.WriteLine(jsonObj.ToString());
            JArray jsonArray = JArray.Parse(jsonObj.GetValue("data").ToString());



            foreach (JObject item in jsonArray)
            {
                comboBox.Items.Add(item.GetValue(name).ToString() + " (" + item.GetValue(phoneNum).ToString() + ")");

            }

            comboBox.SelectedIndex = 0;
        }

        private string GetValue(JObject item, string str)
        {
            return (item.GetValue(str) == null) ? "" : item.GetValue(str).ToString();
        }
        private void makeDataGrid()
        {

            string str = makeSQL("name,phone_num,category,app,", ",phone_num,app");

            string result = request(url, str);

            JObject jsonObj = JObject.Parse(result);
            JArray jsonArray = JArray.Parse(jsonObj.GetValue("data").ToString());
            //chart.Series["Series"].Points.Clear();
            dataTable.Rows.Clear();
            dataTable.Columns.Clear();


            dataTable.Columns.Add("name", typeof(string));
            dataTable.Columns.Add("phone_num", typeof(string));
            dataTable.Columns.Add("category", typeof(string));
            dataTable.Columns.Add("app", typeof(string));
            dataTable.Columns.Add("data", typeof(string));
            dataTable.Columns.Add("time", typeof(string));


            foreach (JObject item in jsonArray)
            {
                List<string> columns = new List<string>();
                var e = dataTable.Columns.GetEnumerator();
                while (e.MoveNext())
                {
                    columns.Add(GetValue(item, e.Current.ToString()));
                }
                dataTable.Rows.Add(columns.ToArray());
            }

            dataGrid.ItemsSource = dataTable.DefaultView;
        }

        private void makeChart()
        {
            chart.Legends.Clear();
            chart.Series.Clear();
            chart.ChartAreas.Clear();

            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series = new System.Windows.Forms.DataVisualization.Charting.Series();

            ((System.ComponentModel.ISupportInitialize)(chart)).BeginInit();
            // 
            // chart
            // 
            

            chartArea.Name = "ChartArea";
            chart.ChartAreas.Add(chartArea);
            legend.Name = "Legend";
            chart.Legends.Add(legend);
            //chart.Location = new System.Drawing.Point(12, 12);
            chart.Name = "chart";
            series.ChartArea = "ChartArea";
            series.Legend = "Legend";
            series.Name = "Average";
            chart.Series.Add(series);
            //chart.Size = new System.Drawing.Size(474, 300);

            chart.Series["Average"].Points.Clear();
            chart.Series["Average"].IsValueShownAsLabel = true;


            chart.TabIndex = 0;
            chart.Text = "chart";

            Chart.Child = chart;
            string intervalType = dateTypeSwitch_btn.Content.Equals("일별") ? "DATE_FORMAT(data_date, '%Y-%m-%d')" : (dateTypeSwitch_btn.Content.Equals("주별") ? "DATE_FORMAT(data_date,'%Y-%U')" : "MONTH(data_date)");

            string str = makeSQL("phone_num,count(distinct "+ intervalType +") as c,", ",phone_num");
            string sql = "select (data/c) as data, time from (SELECT SUM(data) as data,SUM(c) as c, time FROM (" + str + ")  as tt GROUP BY time ORDER BY time) as t GROUP BY time ORDER BY time";
 //           string sql = "SELECT avg(data) as data, time FROM (SELECT name, sum(use_time/60) AS data, hour AS time FROM user_data_usage WHERE phone_num = '01012345678' AND '2020-08-25'<= data_date AND data_date <='2020-08-27' GROUP BY time ,name ORDER BY time) as tt GROUP BY time ORDER BY time";
            string result = request(url, sql);

            //string str = makeSQL("", "");
            //string result = request(url, str);

            JObject jsonObj = JObject.Parse(result);
            JArray jsonArray = JArray.Parse(jsonObj.GetValue("data").ToString());

            series.ChartType = SeriesChartType.SplineArea;

            String tmp = "";

            foreach (JObject item in jsonArray)
            {
                //string usage = (tmp == "") ? item.GetValue("data").ToString() : Convert.ToString(Convert.ToDouble(item.GetValue("data").ToString()) - Convert.ToDouble(tmp));


                //chart.Series["Average"].Points.AddXY(item.GetValue("time").ToString(), usage);
                //tmp = item.GetValue("data").ToString();


                //chart.Series["Average"].Points.AddXY(item.GetValue("time").ToString(), usage);

                chart.Series["Average"].Points.AddXY(item.GetValue("time").ToString(), String.Format("{0:0.0}",Convert.ToDouble(item.GetValue("data").ToString())));
            }
        }
        private void intervalSwitch(object sender, RoutedEventArgs e)
        {
            switch (intervalSwitch_Btn.Content)
            {
                case "기간별":
                    intervalSwitch_Btn.Content = "시간별";
                    break;
                case "시간별":
                    intervalSwitch_Btn.Content = "기간별";
                    break;
            }
        }

        private void dataTypeSwitch(object sender, RoutedEventArgs e)
        {
            switch (dataTypeSwitch_Btn.Content)
            {
                case "Wifi":
                    dataTypeSwitch_Btn.Content = "Mobile";
                    break;
                case "Mobile":
                    dataTypeSwitch_Btn.Content = "Mobile + WiFi";
                    break;
                case "Mobile + WiFi":
                    dataTypeSwitch_Btn.Content = "사용 시간";
                    break;
                case "사용 시간":
                    dataTypeSwitch_Btn.Content = "Wifi";
                    break;
            }
        }

        private void ChartOrList(object sender, RoutedEventArgs e)
        {


            switch (Chart.Visibility)
            {
                case Visibility.Collapsed:
                    Chart.Visibility = Visibility.Visible;
                    dataGrid.Visibility = Visibility.Collapsed;
                    //listbox.Visibility = Visibility.Visible;
                    chartlist_Btn.Content = "차트 보기";
                    break;

                case Visibility.Visible:
                    Chart.Visibility = Visibility.Collapsed;
                    dataGrid.Visibility = Visibility.Visible;
                    //listbox.Visibility = Visibility.Collapsed;
                    chartlist_Btn.Content = "리스트 보기";
                    break;
            }
        }

        private void MonthOrWeekOrDate(object sender, RoutedEventArgs e)
        {
            switch (dateTypeSwitch_btn.Content)
            {
                case "일별":
                    dateTypeSwitch_btn.Content = "주별";
                    break;
                case "주별":
                    dateTypeSwitch_btn.Content = "월별";
                    break;
                case "월별":
                    dateTypeSwitch_btn.Content = "일별";
                    break;
            }
        }

        private void dataApply(object sender, RoutedEventArgs e)
        {
            

            try
            {
                makeDataGrid();
                makeChart();
                setScreenCount();
            }
            catch (Exception ex)
            {
                //MessageBox.Show(ex.ToString());
                MessageBox.Show("잘못된 접근입니다.");
            }
            //Console.WriteLine(makeSQL());
        }

        private void AppComboBoxUpdate(object sender, EventArgs e)
        {
            if (category.SelectedIndex == 0)
            {
                fillCombobox(application, "SELECT distinct name FROM app", "name");
            }
            else
            {
                string sql = "SELECT name FROM app WHERE category = '" + category.Text + "'";
 //               Console.WriteLine(sql);
                string result = request(url, sql);

                JObject jsonObj = JObject.Parse(result);
                JArray jsonArray = JArray.Parse(jsonObj.GetValue("data").ToString());

                application.Items.Clear();

                application.Items.Add("모두 보기");
                foreach (JObject item in jsonArray)
                {
                    application.Items.Add(item.GetValue("name").ToString());
                }            }
            application.SelectedIndex = 0;
        }

        private void CategoryComboBoxUpdate(object sender, EventArgs e)
        {

                string sql = "SELECT category FROM app WHERE name = '" + application.Text + "'";
                Console.WriteLine(sql);
                string result = request(url, sql);

                JObject jsonObj = JObject.Parse(result);
                JArray jsonArray = JArray.Parse(jsonObj.GetValue("data").ToString());

                string categoryName = "";
                foreach (JObject item in jsonArray)
                {
                    categoryName = item.GetValue("category").ToString();
                }

                foreach (var item in category.Items)
                {
                    if (item.ToString() == categoryName)
                        category.SelectedItem = item;
                }

 
        }
    }
}
