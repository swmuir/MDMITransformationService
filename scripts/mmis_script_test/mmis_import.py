import os
from datetime import datetime
from io import StringIO
from shutil import copyfileobj
from time import sleep

# import google.auth
import requests
from google.cloud import storage

# creds, project = google.auth.default()
# auth_req = google.auth.transport.requests.Request()
# creds.refresh(auth_req)

MDIX1 = os.getenv("MDIX1")
MDIX2 = os.getenv("MDIX2")
BUCKET = os.getenv("BUCKET")
BATCH = 50

ORDERED_FILES1 = [
 
    (
        "Patient_Sample_1.txt",
        f"{MDIX1}/mdmi/transformation/transformAndPost?source=NJ.Person&target=FHIRR4JSON.MasterBundle",
    )
    
   
   
 
    
]

ORDERED_FILES2 = [
 
    
    (
        "Provider_Sample_1.txt",
        f"{MDIX2}/mdmi/transformation/transformAndPost?source=NJ.PROVIDER&target=FHIRR4JSON.MasterBundle",
    ),
    (
        "Claim_Sample_1.txt",
        f"{MDIX2}/mdmi/transformation/transformAndPost?source=NJ.Claim&target=FHIRR4JSON.MasterBundle",
    )
   
 
    
]


def main(source_file_bucket: str = None):
    ts = datetime.utcnow().isoformat()
    storage_client = storage.Client()
    bucket = storage_client.bucket(source_file_bucket)
    for file_name, url in ORDERED_FILES2:
        blob = bucket.blob(f"mmis/Sample_Files/{file_name}") 
        blob.download_to_filename("/tmp/temp.csv")
        with open("/tmp/temp.csv", "r") as txt_file:
            lines = txt_file.readlines()
            header = lines.pop(0).replace("#", "")
            upload_file = StringIO()
            upload_file.write(header)
            urlist_len = len(lines)-1
            for n, line in enumerate(lines):
                # upload_file.write(line.replace("\\", "").replace(">", "&lt;"))
                upload_file.write(line)
                if n and (n % BATCH == 0 or n ==urlist_len):
                    print(f"uploading {n-BATCH} - {n}")
                    while True:  # retry
                        upload_file.seek(0)
                        upload_file_copy = StringIO()
                        copyfileobj(upload_file, upload_file_copy)
                        upload_file_copy.seek(0)
                        response = requests.post(
                            url,
                            # headers={"Content-Type": "application/xml"},
                            files={"message": upload_file_copy},
                        )
                        if response.ok:
                            break
                        elif response.status_code in (504, 500):
                            print(f"retry uploading {n-BATCH} - {n}")
                            sleep(1)
                        else:
                            raise Exception
                    upload_file = StringIO()
                    upload_file.write(header)
    # for file_name, url in ORDERED_FILES:
    #     blob = bucket.blob(f"mmis/{file_name}")
    #     blob_copy = bucket.copy_blob(blob, bucket, f"mmis/processed/{ts}/{file_name}")
    #     bucket.delete_blob(f"mmis/{file_name}")
    return None


if __name__ == "__main__":
    main(BUCKET)
