import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CountersInMapReduce {

	private enum COUNTERS {
		GOOD,
		BAD
	}
	
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//2017 45  *****GOOD 
			//2016 -1  *****BAD
			String lineArr[] = value.toString().split(" ");
			if(lineArr[1] != "-1") 
				context.getCounter(COUNTERS.GOOD).increment(1);
			else
				context.getCounter(COUNTERS.BAD).increment(1);
		}
	} 

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "CountersInMapReduce");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
		
		
		Counter goodRecords = job.getCounters().findCounter(COUNTERS.GOOD);
		Counter badRecords = job.getCounters().findCounter(COUNTERS.BAD);

		System.out.println("Good records=["+goodRecords.getValue()+"]");
		System.out.println("Bad records=["+badRecords.getValue()+"]");

	}

}
