import { MikroORM } from "@mikro-orm/core";
import { CsvReaderService } from "./services/CsvReaderService";
import { PerfumeImportService } from "./services/PerfumeImportService";
import config from "../config/mikro-orm.config";
import logger from "../config/logger";

async function importPerfumeData() {
  const orm = await MikroORM.init(config);

  try {
    logger.info("Starting perfume data import...");

    const em = orm.em.fork();
    const csvReader = new CsvReaderService();
    const importService = new PerfumeImportService(em, csvReader);

    // 브랜드 데이터 가져오기
    // logger.info("Importing brands...");
    const brandMap = await importService.importBrands("brand.csv");
    // logger.info(`Imported ${brandMap.size} brands`);

    // 향수 데이터 가져오기
    logger.info("Importing perfumes...");
    const perfumeMap = await importService.importPerfumes(
      "perfume.csv",
      brandMap
    );
    logger.info(`Imported ${perfumeMap.size} perfumes`);

    // 노트 데이터 가져오기
    // logger.info("Importing notes...");
    // await importService.importNotes("note.csv", perfumeMap);

    // 어코드 데이터 가져오기
    logger.info("Importing accords...");
    await importService.importAccords("main_accord.csv", perfumeMap);

    logger.info("Perfume data import completed successfully!");
  } catch (error) {
    logger.error(`Error during import: ${error}`);
    process.exit(1);
  } finally {
    await orm.close(true);
  }
}

// 배치 작업 실행
importPerfumeData().catch((error) => {
  logger.error(`Unhandled error: ${error}`);
  process.exit(1);
});
