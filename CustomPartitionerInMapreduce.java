
import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/****
 * 
 * @author Shobha
 * Given roll Number and marks
 * This mapreduce finds total marks roll number wise
 *
 */
public class CustomPartitionerInMapreduce {
	public static void main(String args[]) {

		if (args.length != 2) {
			System.err.println("Usage: CustomPartitionerInMapreduce <input path> <output path>");
			System.exit(-1);
		}

		Job job;
		try {
			job = new Job();
			job.setJarByClass(CustomPartitionerInMapreduce.class);
			job.setJobName("CustomPartitionerInMapreduce");
			job.setNumReduceTasks(2);
			
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			
			job.setMapperClass(myMapper.class);
			job.setReducerClass(myReducer.class);
			job.setPartitionerClass(customPartitioner.class);
			job.setOutputKeyClass(IntWritable.class);
			job.setOutputValueClass(IntWritable.class);
			System.exit(job.waitForCompletion(true) ? 0 : 1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}

class myMapper extends Mapper<LongWritable, Text, IntWritable, IntWritable> {
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		int rollNum = Integer.parseInt( line.split(" ")[0]);
		int marks = Integer.parseInt( line.split(" ")[1]);

		context.write(new IntWritable(rollNum), new IntWritable(marks));
	}
}

class customPartitioner  extends Partitioner<IntWritable,IntWritable> {
	@Override
	public int getPartition(IntWritable key, IntWritable value, int noOfReduceTasks) {
		// TODO Auto-generated method stub
		if(noOfReduceTasks == 0)
			return 0;
		else if(key % 2 == 0)
			return 0;
		else
			return 1;
	}
}

class myReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
	@Override
	public void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
			throws IOException, InterruptedException {
		int count = 0;
		for (IntWritable value : values) {
			count += value.get();
		}
		context.write(key, new IntWritable(count));
	}

}
