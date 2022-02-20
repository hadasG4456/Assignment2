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
            Job job1 = Job.getInstance(conf1setup);
            job1.setJarByClass(step1.class);
            job1.setPartitionerClass(step1.PartitionerClass.class);
            job1.setMapperClass(step1.Map.class);
            job1.setReducerClass(step1.Reduce.class);
            job1.setOutputKeyClass(Text.class);
            job1.setOutputValueClass(Text.class);
            job1.setOutputFormatClass(TextOutputFormat.class);
            job1.setInputFormatClass(SequenceFileInputFormat.class);
            SequenceFileInputFormat.addInputPath(job1, new Path(args[0]));
            String output = "s3://assignment2dsp/outputStep1/";
            FileOutputFormat.setOutputPath(job1, new Path(output));
            job1.waitForCompletion(true);

            //        ------------------------------------

            Configuration conf2 = new Configuration();
            Job job2 = Job.getInstance(conf2);
            job2.setJarByClass(step2.class);
            job2.setMapperClass(step2.Map.class);
            job2.setReducerClass(step2.Reduce.class);
            job2.setOutputKeyClass(Text.class);
            job2.setOutputValueClass(Text.class);
            job2.setPartitionerClass(step2.PartitionerClass.class);
            job2.setOutputFormatClass(TextOutputFormat.class);
            String output4 = "s3://assignment2dsp/outputStep2/";
            job2.setInputFormatClass(TextInputFormat.class);
            FileInputFormat.addInputPath(job2, new Path(output));
            FileOutputFormat.setOutputPath(job2, new Path(output4));
            job2.waitForCompletion(true);

            //        ------------------------------------

            Configuration conf3 = new Configuration();
            Job job3 = Job.getInstance(conf3);
            job3.setJarByClass(step3.class);
            job3.setOutputKeyClass(Text.class);
            job3.setOutputValueClass(Text.class);
            job3.setMapperClass(step3.Map.class);
            job3.setSortComparatorClass(step3.CompareClass.class);
            job3.setReducerClass(step3.Reduce.class);
            job3.setNumReduceTasks(1);
            job3.setInputFormatClass(TextInputFormat.class);
            job3.setOutputFormatClass(TextOutputFormat.class);
            FileInputFormat.addInputPath(job3, new Path(output4));
            FileOutputFormat.setOutputPath(job3, new Path(args[1]));
            job3.waitForCompletion(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
