package com.mapreduce.examples;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TotalAverageSalaryMapReduce {
	/*
	 * data :-
	 * M 100000
	 * M 332323
	 * F 323232
	 * F 150000
	 */
	public static class MapperClass extends Mapper<LongWritable, Text, Text, FloatWritable> {
		public void map(LongWritable key, Text empRecord, Context context)
				throws IOException, InterruptedException {
			String[] word = empRecord.toString().split("\\t");
			String sex = word[0];
			try {
				Float salary = Float.parseFloat(word[1]);
				context.write(new Text(sex), new FloatWritable(salary));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class ReducerClass extends Reducer<Text, FloatWritable, Text, Text> {
		public void reduce(Text key, Iterable<FloatWritable> valueList,
				Context context) throws IOException, InterruptedException {
			try {
				Float total = (float) 0;
				int count = 0;
				for (FloatWritable var : valueList) {
					total += var.get();
					count++;
				}
				Float avg = (Float) total / count;
				String out = "Total: " + total + " :: " + "Average: " + avg;
				context.write(key, new Text(out));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Configuration conf = new Configuration();
		try {
			if (args.length != 2) {
				System.err.println("Usage: TotalAverageSalaryMapReduce <input path> <output path>");
				System.exit(-1);
			}
			Job job = Job.getInstance(conf, "TotalAverageSalaryMapReduce");
			job.setJarByClass(TotalAverageSalaryMapReduce.class);
			job.setMapperClass(MapperClass.class);
			job.setReducerClass(ReducerClass.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(FloatWritable.class);
			Path pathInput = new Path(args[0]);
			Path pathOutputDir = new Path(args[1]);
			FileInputFormat.addInputPath(job, pathInput);
			FileOutputFormat.setOutputPath(job, pathOutputDir);
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
