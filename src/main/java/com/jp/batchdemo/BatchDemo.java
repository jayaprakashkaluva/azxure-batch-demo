package com.jp.batchdemo;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.microsoft.azure.batch.BatchClient;
import com.microsoft.azure.batch.auth.BatchCredentials;
import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudPool;
import com.microsoft.azure.batch.protocol.models.ImageInformation;
import com.microsoft.azure.batch.protocol.models.ImageReference;
import com.microsoft.azure.batch.protocol.models.OSType;
import com.microsoft.azure.batch.protocol.models.PoolAddParameter;
import com.microsoft.azure.batch.protocol.models.VerificationType;
import com.microsoft.azure.batch.protocol.models.VirtualMachineConfiguration;

public class BatchDemo {
	public static void main(String[] args) {

		String storageAccountName = System.getenv("STORAGE_ACCOUNT_NAME");
		String storageAccountKey = System.getenv("STORAGE_ACCOUNT_KEY");

		Boolean shouldDeleteContainer = true;
		Boolean shouldDeleteJob = true;
		Boolean shouldDeletePool = false;

		Duration TASK_COMPLETE_TIMEOUT = Duration.ofMinutes(1);
		String STANDARD_CONSOLE_OUTPUT_FILENAME = "stdout.txt";

	}

	private static BatchSharedKeyCredentials getCredentials() {
		String batchAccount = System.getenv("AZURE_BATCH_ACCOUNT");
		String batchKey = System.getenv("AZURE_BATCH_ACCESS_KEY");
		String batchUri = System.getenv("AZURE_BATCH_ENDPOINT");
		return new BatchSharedKeyCredentials(batchUri, batchAccount, batchKey);
	}

	private static BatchClient getBatchClient() {
		return BatchClient.open(getCredentials());
	}

	private static void createCloudPool(String poolId) throws BatchErrorException, IOException {
        String poolVMSize = "STANDARD_A1";
        int poolVMCount =1;
		getBatchClient().poolOperations().createPool(poolId,poolVMSize,getVmCofiguration(),poolVMCount);
	}

	private static void createPoolIfDoesNotExist(String poolId) throws BatchErrorException, IOException {
		if (!doesPoolExists(poolId)) {
			createCloudPool(poolId);
		}
	}

	private static boolean doesPoolExists(String poolId) {
		try {
			return getBatchClient().poolOperations().existsPool(poolId);
		} catch (BatchErrorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static  VirtualMachineConfiguration getVmCofiguration() throws BatchErrorException, IOException {
		List<ImageInformation> skus = getBatchClient().accountOperations().listSupportedImages();
        String skuId = null;
        ImageReference imageRef = null;
        String osPublisher = "OpenLogic";
        String osOffer = "CentOS";
        for (ImageInformation sku : skus) {
            if (sku.osType() == OSType.LINUX) {
                if (sku.verificationType() == VerificationType.VERIFIED) {
                    if (sku.imageReference().publisher().equalsIgnoreCase(osPublisher)
                            && sku.imageReference().offer().equalsIgnoreCase(osOffer)) {
                        imageRef = sku.imageReference();
                        skuId = sku.nodeAgentSKUId();
                        break;
                    }
                }
            }
        }

        // Use IaaS VM with Linux
        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
        configuration.withNodeAgentSKUId(skuId).withImageReference(imageRef);
        
        return null;
	}
}