package com.amazon.chayel.AmazonEMR;


import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class WordCountMapper extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable ONE = new IntWritable(1);
    private Text word = new Text();

    @Override
    protected void map(Object notUsed, Text value, Context context) throws IOException, InterruptedException {
        StringTokenizer itr = new StringTokenizer(value.toString());
        while (itr.hasMoreTokens()) {
            word.set(itr.nextToken());
            context.write(word, ONE);
        }
    }
}