import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class step1 {

    public static class Map extends Mapper<LongWritable, Text, Text, Text> {

        private static final Pattern Hebrew = Pattern.compile("(?<trigram>[א-ת]+ [א-ת]+ [א-ת]+)\\t\\d{4}\\t(?<occurrences>\\d+).*");

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            Matcher matcher = Hebrew.matcher(value.toString());
            if (matcher.matches()) {
                String[] strings = value.toString().split("\t");
                String[] words = strings[0].split(" ");
                if (words.length > 2) {
                    String w1 = words[0];
                    String w2 = words[1];
                    String w3 = words[2];
                    String occur = strings[2];

                    Text textSingle1 = new Text();
                    textSingle1.set(String.format("%s", w1));
                    Text textSingle2 = new Text();
                    textSingle2.set(String.format("%s", w2));
                    Text textSingle3 = new Text();
                    textSingle3.set(String.format("%s", w3));
                    Text textTotal = new Text();
                    String all = "ALL";
                    textTotal.set(String.format("%s", all));
                    Text textCouple1 = new Text();
                    textCouple1.set(String.format("%s %s", w1, w2));
                    Text textCouple2 = new Text();
                    textCouple2.set(String.format("%s %s", w2, w3));
                    Text textTriple = new Text();
                    textTriple.set(String.format("%s %s %s", w1, w2, w3));

                    Text textOccur = new Text();
                    textOccur.set(String.format("%s %s %s %s", w1, w2, w3, occur));
                    Text textOccurThree = new Text();
                    textOccurThree.set(String.format("%s", occur));

                    context.write(textSingle1, textOccur);
                    context.write(textSingle2, textOccur);
                    context.write(textSingle3, textOccur);
                    context.write(textTotal, textOccur);
                    context.write(textCouple1, textOccur);
                    context.write(textCouple2, textOccur);
                    context.write(textTriple, textOccurThree);

                }
            }
        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        Set<String> triples = new HashSet<>();
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String oldKey = key.toString();
            int sum = 0;
            boolean three = false;
            for (Text value : values) {
                String[] strings = value.toString().split(" ");
                if (strings.length == 4) {
                    sum += Integer.parseInt(strings[3]);
                    triples.add(strings[0] + "A" + strings[1] + "A" + strings[2]);
                }
                if (strings.length == 1) {
                    sum += Integer.parseInt(strings[0]);
                    three = true;
                }
            }
            if (three) {
                Text Key = new Text();
                Key.set(String.format("%s", oldKey));
                Text Val = new Text();
                Val.set(String.format("%s", sum));
                context.write(Key, Val);
            }
            else {
                for (String triple : triples) {
                    String[] words = triple.split("A");
                    String w1 = words[0];
                    String w2 = words[1];
                    String w3 = words[2];
                    Text Key = new Text();
                    Key.set(String.format("%s %s %s", w1, w2, w3));
                    Text Val = new Text();
                    Val.set(String.format("%s %s", oldKey, sum));
                    context.write(Key, Val);
                }
                triples.clear();
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
