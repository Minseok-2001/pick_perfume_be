import * as fs from "fs";
import * as path from "path";
import csv from "csv-parser";
import logger from "../../config/logger";

export class CsvReaderService {
  async readCsv<T>(filePath: string): Promise<T[]> {
    const results: T[] = [];

    try {
      const fullPath = path.resolve(process.env.CSV_DIR || "../csv", filePath);
      logger.info(`Reading CSV file: ${fullPath}`);

      return new Promise((resolve, reject) => {
        fs.createReadStream(fullPath)
          .pipe(csv())
          .on("data", (data: T) => results.push(data))
          .on("end", () => {
            logger.info(
              `CSV file read successfully. Total records: ${results.length}`
            );
            resolve(results);
          })
          .on("error", (error) => {
            logger.error(`Error reading CSV file: ${error.message}`);
            reject(error);
          });
      });
    } catch (error) {
      logger.error(`Failed to read CSV file: ${error}`);
      throw error;
    }
  }
}
