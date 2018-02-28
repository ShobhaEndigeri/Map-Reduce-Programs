package com.mapreduce.examples.customInput;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class WebLogReader {
	 public static void main(String[] args) throws Exception 
	 {
	      Configuration conf = new Configuration();
	      Job job = new Job();
	      job.setJobName("WebLog Reader");
	 
	      job.setJarByClass(WebLogReader.class);
	 
	      job.setMapperClass(WebLogMapper.class);
	      job.setReducerClass(WebLogReducer.class);
	 
	 
	      job.setOutputKeyClass(Text.class);
	      job.setOutputValueClass(IntWritable.class);
	 
	      job.setMapOutputKeyClass(WebLogWritable.class);
	      job.setMapOutputValueClass(IntWritable.class);
	 
	      FileInputFormat.addInputPath(job, new Path(args[0]));
	      FileOutputFormat.setOutputPath(job, new Path(args[1]));
	 
	      System.exit(job.waitForCompletion(true) ? 0 : 1);
	 }
}

 class WebLogMapper extends Mapper <LongWritable, Text, WebLogWritable, IntWritable>
{
	private static final IntWritable one = new IntWritable(1);

	private WebLogWritable wLog = new WebLogWritable();

	private IntWritable reqno = new IntWritable();
	private Text url = new Text();
	private Text rdate = new Text();
	private Text rtime = new Text();
	private Text rip = new Text();

	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException 
	{
		String[] words = value.toString().split("\t") ;

		reqno.set(Integer.parseInt(words[0]));
		url.set(words[1]);
		rdate.set(words[2]);
		rtime.set(words[3]);
		rip.set(words[4]);

		wLog.set(reqno, url, rdate, rtime, rip);

		context.write(wLog, one);
	}
}
 
  class WebLogReducer extends Reducer <WebLogWritable, IntWritable, Text, IntWritable>
 {
 	private IntWritable result = new IntWritable();
 	private Text ip = new Text();

 	public void reduce(WebLogWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
 	{
 		int sum = 0;
 		ip = key.getIp(); 

 		for (IntWritable val : values) 
 		{
 			sum++ ;
 		}
 		result.set(sum);
 		context.write(ip, result);
 	}
 }
