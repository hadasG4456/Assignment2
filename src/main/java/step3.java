import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class step3 {

    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map (LongWritable key, Text value, Context context)  throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            Text key1 = new Text();
            key1.set(String.format("%s %s",splits[0], splits[1]));
            Text newValue = new Text();
            newValue.set("");
            context.write(key1,newValue);
        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String w1 = key.toString();
            Text newKey = new Text();
            newKey.set(String.format("%s",w1));
            Text newVal = new Text();
            newVal.set(String.format("%s",""));
            context.write(newKey, newVal);
        }

    }

    public static class CompareClass extends WritableComparator {
        protected CompareClass() {
            super(Text.class, true);
        }
        @Override
        public int compare(WritableComparable key1, WritableComparable key2) {
            String[] splits1 = key1.toString().split(" ");
            String[] splits2 = key2.toString().split(" ");
            if (splits1[0].equals(splits2[0])&& splits1[1].equals(splits2[1])) {
                if(Double.parseDouble(splits1[3])>(Double.parseDouble(splits2[3]))){
                    return -1;
                }
                else
                    return 1;
            }
            return (splits1[0]+" "+splits1[1]).compareTo(splits2[0]+" "+splits2[1]);
        }
    }
}
