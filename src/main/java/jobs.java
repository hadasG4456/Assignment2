import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.log4j.BasicConfigurator;

public class jobs {

    public static void main(String[] args) {
        try {
            BasicConfigurator.configure();
            Configuration conf1setup = new Configuration();
            Job job1setup = Job.getInstance(conf1setup);
            job1setup.setJarByClass(step1.class);
            job1setup.setPartitionerClass(step1.PartitionerClass.class);
            job1setup.setMapperClass(step1.Map.class);
            job1setup.setReducerClass(step1.Reduce.class);
            job1setup.setOutputKeyClass(Text.class);
            job1setup.setOutputValueClass(Text.class);
            job1setup.setOutputFormatClass(TextOutputFormat.class);
            job1setup.setInputFormatClass(SequenceFileInputFormat.class);
            SequenceFileInputFormat.addInputPath(job1setup, new Path(args[0]));
            String output = "s3://assignment2dsp/output1/";
            FileOutputFormat.setOutputPath(job1setup, new Path(output));
            job1setup.waitForCompletion(true);

            //        ------------------------------------

            Configuration conf4 = new Configuration();
            Job job4 = Job.getInstance(conf4);
            job4.setJarByClass(step2.class);
            job4.setMapperClass(step2.Map.class);
            job4.setReducerClass(step2.Reduce.class);
            job4.setOutputKeyClass(Text.class);
            job4.setOutputValueClass(Text.class);
            job4.setPartitionerClass(step2.PartitionerClass.class);
            job4.setOutputFormatClass(TextOutputFormat.class);
            String output4 = "s3://assignment2dsp/output4Com/";
            job4.setInputFormatClass(TextInputFormat.class);
            FileInputFormat.addInputPath(job4, new Path(output));
            FileOutputFormat.setOutputPath(job4, new Path(output4));
            job4.waitForCompletion(true);

            //        ------------------------------------

            Configuration conf5 = new Configuration();
            Job job5 = Job.getInstance(conf5);
            job5.setJarByClass(step3.class);
            job5.setOutputKeyClass(Text.class);
            job5.setOutputValueClass(Text.class);
            job5.setMapperClass(step3.Map.class);
            job5.setSortComparatorClass(step3.CompareClass.class);
            job5.setReducerClass(step3.Reduce.class);
            job5.setNumReduceTasks(1);
            job5.setInputFormatClass(TextInputFormat.class);
            job5.setOutputFormatClass(TextOutputFormat.class);
            FileInputFormat.addInputPath(job5, new Path(output4));
            FileOutputFormat.setOutputPath(job5, new Path(args[1]));
            job5.waitForCompletion(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
