const s3Util = require('./s3-util'),
  path = require('path'),
  convert = require('./convert'),
  os = require('os'),
  EXTENSION = process.env.EXTENSION,
  OUTPUT_BUCKET = process.env.RESULTS_BUCKET_NAME,
  MIME_TYPE = process.env.MIME_TYPE;

exports.handler = function (event, context) {
  console.log(event);
  const inputBucket = event.Records[0].s3.bucket.name,
    key = event.Records[0].s3.object.key,
    id = context.awsRequestId,
    resultKey = key.replace(/\.[^.]+$/, EXTENSION),
    tempPath = path.join(os.tmpdir(), id),
    convertedPath = path.join(os.tmpdir(), 'converted-' + id + EXTENSION);

  console.log('converting', inputBucket, key, 'using', tempPath);
  return s3Util
    .downloadFileFromS3(inputBucket, key, tempPath)
    .then(() => convert(tempPath, convertedPath))
    .then(() =>
      s3Util.uploadFileToS3(OUTPUT_BUCKET, resultKey, convertedPath, MIME_TYPE)
    );
};
