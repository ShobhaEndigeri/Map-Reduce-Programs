
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class CountLinesMapReduce {
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println("Usage: CountLinesMapReduce <input path> <output path>");
			System.exit(-1);
		}
		Job job = new Job();
		job.setJarByClass(CountLinesMapReduce.class);
		job.setJobName("CountLinesMapReduce");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setMapperClass(CountLinesMapper.class);
		job.setReducerClass(CountLinesReducer.class);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
	}
}


class CountLinesMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	Text totalNoofLines = new Text("totalNoofLines");
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		context.write(totalNoofLines, new IntWritable(1));
	}
}


 class CountLinesReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
	@Override
	public void reduce(Text key, Iterable<IntWritable> values, Context context)
			throws IOException, InterruptedException {
		int count = 0;
		for (IntWritable value : values) {
			count = count + value.get();
		}
		context.write(key, new IntWritable(count));
	}
}