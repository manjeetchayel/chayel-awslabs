package com.amazon.chayel.AmazonEMR;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WordCount extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new WordCount(), args);
        System.exit(exitCode);
    }

    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.printf("Usage: %s [generic options] <input> <output>\n",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }

        Job job = Job.getInstance();
        job.setJarByClass(WordCount.class);
        job.setJobName("WordCounter");

        Configuration conf = job.getConfiguration();
     //   conf.set("fs.s3n.awsAccessKeyId", "aws_access_key");
       // conf.set("fs.s3n.awsSecretAccessKey", "aws_secret_access_key");
        //conf.set("fs.defaultfs", "s3n://bucket_name");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Define Output Pair classes for Mappers & Reducers
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Set Input & Output data formats
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Set Mapper & Reducers classes that will be executed by WordCount job
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);

        // Will force MR to execute only one Reducer
        job.setNumReduceTasks(1);

        // Wait until our job ends
        int returnValue = job.waitForCompletion(true) ? 0 : 1;
        System.out.println("job.isSuccessful " + job.isSuccessful());
        return returnValue;
    }
}