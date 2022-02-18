import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


public class Main {
    public static AWSCredentialsProvider credentialsProvider;
    public static AmazonS3 S3;
    public static AmazonEC2 ec2;
    public static AmazonElasticMapReduce emr;
    public static void main(String[]args){

        credentialsProvider =new ProfileCredentialsProvider();

        System.out.println("===========================================");
        System.out.println("connect to aws & S3");
        System.out.println("===========================================\n");

        S3 = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();

        System.out.println("===========================================");
        System.out.println("connect to aws & ec2");
        System.out.println("===========================================\n");

        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();

        System.out.println("creating a emr");
        emr= AmazonElasticMapReduceClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();


        HadoopJarStepConfig hadoopJarStepConfig = new HadoopJarStepConfig()
                .withJar("s3://assignment2dsp/DSP-2.jar")
                .withMainClass("jobs")
                .withArgs("s3n://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/3gram/data"
                        ,"s3n://assignment2dsp/output/");

        StepConfig stepConfig = new StepConfig()
                .withHadoopJarStep(hadoopJarStepConfig)
                .withName("steps")
                .withActionOnFailure("TERMINATE_JOB_FLOW");

        JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
                .withInstanceCount(3)
                .withMasterInstanceType(InstanceType.M4Large.toString())
                .withSlaveInstanceType(InstanceType.M4Large.toString())
                .withHadoopVersion("2.7.3")
                .withEc2KeyName("vockey")
                .withPlacement(new PlacementType("us-east-1a"))
                .withKeepJobFlowAliveWhenNoSteps(false);

        RunJobFlowRequest request = new RunJobFlowRequest()
                .withName("Assignment2")
                .withInstances(instances)
                .withSteps(stepConfig)
                .withLogUri("s3n://assignment2dsp/logs/")
                .withServiceRole("EMR_DefaultRole")
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withReleaseLabel("emr-5.11.0");

        RunJobFlowResult result = emr.runJobFlow(request);
        String id=result.getJobFlowId();
        System.out.println("our cluster id: "+id);
    }
}

