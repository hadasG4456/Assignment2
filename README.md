# Knowledge-base for Word Prediction
knowledge-base for Hebrew word-prediction system, based on Google 3-Gram Hebrew dataset, using Amazon Elastic Map-Reduce (EMR).

## Introduction
We will generate a knowledge-base for Hebrew word-prediction system, based on Google 3-Gram Hebrew dataset, using Amazon
Elastic Map-Reduce (EMR). The produced knowledge-base indicates for a each pair of words the probability of their possible
next words.

## Goal
Our goal is to  build a map-reduce system to calculate the conditional probability for each trigram (w1,w2,w3) found in
the corpus:  Hebrew 3-Gram dataset of [Google Books Ngrams](https://aws.amazon.com/datasets/google-books-ngrams/).

The probability calculate the probability that w3 will appear after the two given words- w1,w2 and calculated according to
[Thede & Harper](https://dl.acm.org/doi/10.3115/1034678.1034712) :
<p>
  <img src="https://github.com/eladshamailov/Assignment2/blob/master/probability%20Calculation.png?raw=true"/>
</p>

The output of the system is a list of word trigrams (w1,w2,w3) and their conditional probabilities P(w3|w1,w2).
The list will be ordered: 
1. by w1w2, ascending; 
2. by the probability for w3, descending.

For example:
```
קפה נמס עלית 0.6
קפה נמס מגורען 0.4

קפה שחור חזק 0.6

קפה שחור טעים 0.3

קפה שחור חם 0.1

…

שולחן עבודה ירוק 0.7

שולחן עבודה מעץ 0.3

…
```
## Team
Hadas Gilon

Zohar Levi

## Instruction
1. Create s3 bucket called assignment2dsp.
2. Upload DSP-2.jar to assignment2dsp bucket.
3. Run the jar -jar DSP-2.jar.

## Description
Our application has 1 step with 3 rounds of MapReduce.
We used 3 M4Large instance.

### Input

The sequence file key is the row number of the dataset stored as a LongWritable and the value is the raw data stored as TextWritable.

The value is a tab separated string containing the following fields:

* 3-gram - The actual 3-gram
* year - The year for this aggregation
* occurrences - The number of times this 3-gram appeared in this year
* pages - The number of pages this 3-gram appeared on in this year
* books - The number of books this 3-gram appeared in during this year

The 3-gram field is a space separated representation of the tuple.

### Map Reduce jobs:

#### Job1:

Job1 takes as input the 3-gram corpus and parses it line by line.

For each line from the 3-gram corpus (w1, w2, w3), it creates 7 lines with the according information:
 1. Each single word and the occurrence of the triple (it is the same occurrence of the word in the triple) and the
triple it came from - 3 lines.
 2. Each couple of words that are adjacent - (w1,w2), (w2,w3) and the occurrence of the triple and the triple it came from - 
2 lines.
 3. The triple itself and it's occurrence.
 4. A special line with the word "ALL" that symbolized how many word are there in the corpus with the occurrence and the triple.

In the reducer , we combine all the occurrences by key. Note that we also sum up all the occurrences with * in order to
get the sum of all the words occurrences. After we got all the total occurrences- we switch the key to be the triple and each value
is all the permutations of the triple and it's occurrences.

At the end of this job, each key (triple) contains all the information it needs to calculate the probability in its values
iterator.

### Job2:

Job2 takes as input Job1's output.

The map function arrange the work space for the reducer and assign the key to be the triple and the value is either the single
words, the couples, the triple itself or the special word "ALL" and for each add their total occurrence.

All the information is sent to the reducer.

In the reducer, we calculate the probability of the appearance of 3 words in the text According to the formula above:

<p>
  <img src="https://github.com/eladshamailov/Assignment2/blob/master/probability%20Calculation.png?raw=true"/>
</p>

Where:
* N1 is the number of times w3 occurs.
* N2 is the number of times sequence (w2,w3) occurs.
* N3 is the number of times sequence (w1,w2,w3) occurs.
* C0 is the total number of word instances in the corpus.
* C1 is the number of times w2 occurs.
* C2 is the number of times sequence (w1,w2) occurs.

According to the data we keep for each triplet we take the information from the value iterator of each key:
* N1 - from the permutation where only w3 is in with its total number of appearance in the corpus.
* N2 - from the permutation where only the couple (w2,w3) is in with its total number of appearance in the corpus.
* N3 - from the permutation where only the triple itself (w1,w2,w3) is in with its total number of appearance in the corpus.
* C0 - from special word "ALL" with its total number of appearance in the corpus.
* C1 - from the permutation where only w2 is in with its total number of appearance in the corpus.
* C2 - from the permutation where only the couple (w1,w2) is in with its total number of appearance in the corpus.

then , the key is the 3 words and the value is the probability.

** note that we have deleted the probabilities that where bigger than 1 or smaller than 0.

### Job3:

Job3 takes as input the output of Job2.

In the mapper, the input is the same key and value sorted by decending value of the probability. It puts the probability 
as a part of the key for the comparator.

In the compare function we compare with the CompareClass two strings from the key, if the first two words are the same,
we return which one has the higher probability.

The reducer (only one reducer) gets the sorted keys and write them by their order to the output file.

### Main:

The main includes all the steps and the arguments that we need - the 3-gram database and the output folder in the s3.
It starts all the instances we need for the Map Reduce and the stepConfig where we create the instruction to start all the 
jobs and arguments, and we let the application run.

### Jobs:

Start the configuration for each job and the job itself with the input, output and parameters for every job. Starting each job
by their order and wait for each one to finish.

## Statistics

### Job1:

```

Map input records=163471963

Map output records=497836591

Map output bytes=17738105838

Combine input records=0

Combine output records=0

Reduce input records=497836591

Reduce output records=11791253

```

### Job2:

```

Map input records=11791253

Map output records=11791253

Map output bytes=476063090

Combine input records=0

Combine output records=0

Reduce input records=11791253

Reduce output records=704442

```

### Job3:

```

Map input records=704442

Map output records=704442

Map output bytes=32137378

Combine input records=0

Combine output records=0

Reduce input records=704442

Reduce output records=704442

```

## Analysis

###example 1:

the pair is: "אחדותה של"

```

אחדותה של האומה 0.7491929266980898	

אחדותה של התנועה 0.32184736676875525
	
אחדותה של תנועת 0.27632925694413085	

אחדותה של היצירה 0.20016440324775062
	
אחדותה של הקהילה 0.10246656858965805

```

###example 2:


the pair is: "היא נכנסה"

```
היא נכנסה פנימה 0.26655283036889704	

היא נכנסה לבית 0.24657857420114374	

היא נכנסה למטבח 0.2043880968724656	

היא נכנסה הביתה 0.16648478479330978	

היא נכנסה למיטה 0.14308458502586519

```

###example 3:


the pair is: "יש כאן"

```

יש כאן כי 0.7987443408946345	

יש כאן גם 0.6582751669562893	

יש כאן הוא 0.5137630061404663	

יש כאן מה 0.49547621882111953	

יש כאן אם 0.4624554464287052

```


###example 4:


the pair is: "לא היה"

```

לא היה אלא 0.8524770950684452	

לא היה עם 0.8201732762489152	

לא היה ולא 0.6886359245523637	

לא היה מן 0.622197130071587	

לא היה גם 0.6138239711795211

```


###example 5:


the pair is: "את התשובות"

```

את התשובות . 0.08220866570451765	

את התשובות לשאלות 0.06364764039393662	

את התשובות על 0.042019033991377255	

את התשובות הנכונות 0.02915666004956438	

את התשובות של 0.02176840608258283

```


###example 6:


the pair is: "מצאה לה"

```

מצאה לה ביטוי 0.6381673720291411	

מצאה לה גם 0.34923514828607344	

מצאה לה מהלכים 0.30709516997595265	

מצאה לה מקום 0.23148861730344933	

מצאה לה הד 0.1748826738649832

```
Interestingly- If we look at the next example and compare them- we get different results depending on the gender.
```
מצאה לה תומכים 0.12286740660173824	

מצאה לה עבודה 0.05142314815034794
	
מצאה לה מנוחה 0.02693120249360301	

```


###example 7:


the pair is: "מצא לו"

```
מצא לו גם 0.5834669278898588	

מצא לו מקום 0.5312415792094007	

מצא לו ביטוי 0.1894708685417033	

מצא לו זמן 0.17021549429903277	

מצא לו בכל 0.16414372100322966
```
There are not many overlapping words, but those who does- have variety of probability differences.
```
מצא לו תומכים 0.03404367308069964	

מצא לו עבודה 0.07546291869221061

מצא לו מנוחה 0.027444344579383276	
```


###example 8:


the pair is: "פעם אחת"

```

פעם אהת היה 0.6687966825360198	

פעם אהת הוא 0.4033248426435527	

פעם אהת היו 0.1257604687174557	

פעם אהת אמר 0.11765250822948208	

פעם אהת בכל 0.06076915699083378

```


###example 9:


the pair is: "שבת אחרי"

```

שבת אחרי הצהרים 0.5775616849346707	

שבת אחרי הצהריים 0.3874684299537327	

שבת אחרי הסעודה 0.25778686164040454	

שבת אחרי ההבדלה 0.2543138730555782	

שבת אחרי התפילה 0.19441938062727301

```


###example 10:


the pair is: "תשובתו של"

```
תשובתו של כל 0.9953785918312897	

תשובתו של אחד 0.3990719265023504	

תשובתו של רב 0.3021349884588773	

תשובתו של בן 0.30101379506429754	

תשובתו של היא 0.26372582466075806
```


## Scalability

We are supporting larger inputs as well, since we have minor local memory saves- the triple for each permutation,
and we clear it for every key.
