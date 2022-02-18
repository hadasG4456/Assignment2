import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class step2 {

    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] strings = value.toString().split("\t");
            String[] keys = strings[0].split(" ");
            Text key1 = new Text();
            key1.set(String.format("%s %s %s",keys[0], keys[1], keys[2]));       //w1 w2 w3
            Text valu = new Text();
            String[] val = strings[1].split(" ");
            if (val.length == 1) {
                valu.set(String.format("%s", val[0]));      //occurrence of the triple
            }
            if (val.length == 2) {
                valu.set(String.format("%s %s", val[0], val[1]));       //val[1] is the occurrence of the single or the total
            }
            if (val.length == 3) {
                valu.set(String.format("%s %s %s", val[0], val[1], val[2]));        //val[2] is the occurrence of the doubles
            }
            context.write(key1, valu);
        }
    }


    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
            String[] triple = key.toString().split(" ");
            String w1 = triple[0];
            String w2 = triple[1];
            String w3 = triple[2];
            double N1 = 0.0;
            double N2 = 0.0;
            double N3 = 0.0;
            double C0 = 0.0;
            double C1 = 0.0;
            double C2 = 0.0;
            double k2 = 0.0;
            double k3 = 0.0;
            double prob;
            boolean logk2 = false;
            boolean logk3 = false;
            for (Text val : values) {
                String[] value = val.toString().split(" ");
                if (value.length == 1) {
                    N3 = Double.parseDouble(value[0]);
                    k3 = (Math.log(N3 + 1) + 1 / (Math.log(N3 + 1) + 2));
                    logk3 = true;
                }
                if (value.length == 2) {
                    if (value[0].equals("ALL")) {
                        C0 = Double.parseDouble(value[1]);
                    }
                    if (value[0].equals(w2)) {
                        C1 = Double.parseDouble(value[1]);
                    }
                    if (value[0].equals(w3)) {
                        N1 = Double.parseDouble(value[1]);
                    }
                }
                if (value.length == 3) {
                    if (value[0].equals(w1) && value[1].equals(w2)) {
                        C2 = Double.parseDouble(value[2]);
                    }
                    if (value[0].equals(w2) && value[1].equals(w3)) {
                        N2 = Double.parseDouble(value[2]);
                        k2 = (Math.log(N2 + 1) + 1 / (Math.log(N2 + 1) + 2));
                        logk2 = true;
                    }
                }
            }
            if (logk2 && logk3) {
                prob = (k3 * (N3 / C2)) + ((1 - k3) * k2 * (N2 / C1)) + ((1 - k3) * (1 - k2) * (N1 / C0));
                Text Triple = new Text();
                Triple.set(String.format("%s %s %s", w1, w2, w3));
                Text val = new Text();
                val.set(String.format("%s", prob));
                context.write(Triple, val);
            }
        }
    }

    public static class PartitionerClass extends Partitioner<Text, Text> {
        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            return Math.abs(key.hashCode()) % numPartitions;
        }
    }
}
