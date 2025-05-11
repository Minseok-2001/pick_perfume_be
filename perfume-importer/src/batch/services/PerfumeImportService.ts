import { EntityManager } from "@mikro-orm/core";
import { CsvReaderService } from "./CsvReaderService";
import logger from "../../config/logger";
import { PerfumeCsv, BrandCsv, NoteCsv, AccordCsv } from "../models/csv-models";
import { Brand } from "../../entities/Brand";
import { Perfume, Concentration } from "../../entities/Perfume";
import { Note } from "../../entities/Note";
import { Accord } from "../../entities/Accord";
import { PerfumeNote, NoteType } from "../../entities/PerfumeNote";
import { PerfumeAccord } from "../../entities/PerfumeAccord";
import { Designer } from "../../entities/Designer";
import { PerfumeDesigner, DesignerRole } from "../../entities/PerfumeDesigner";

export enum Gender {
  MALE = "MALE",
  FEMALE = "FEMALE",
  UNISEX = "UNISEX",
}

export class PerfumeImportService {
  constructor(
    private readonly em: EntityManager,
    private readonly csvReader: CsvReaderService
  ) {}

  async importBrands(filePath: string): Promise<Map<string, Brand>> {
    const brandMap = new Map<string, Brand>();

    try {
      const brands = await this.csvReader.readCsv<BrandCsv>(filePath);
      logger.info(`Importing ${brands.length} brands...`);

      for (const brandCsv of brands) {
        if (!brandCsv.name || brandCsv.name.trim() === "") {
          logger.warn(`Empty brand name, skipping...`);
          continue;
        }

        const brandName = brandCsv.name.trim();
        const brandNameLower = brandName.toLowerCase();

        // 기존 브랜드 검색 - 이름으로 찾기
        let brand = await this.em.findOne(Brand, {
          name: { $eq: brandName },
        });

        // 정확히 일치하는 항목이 없으면 대소문자 구분 없이 검색
        if (!brand) {
          const brands = await this.em.find(Brand, {});
          for (const b of brands) {
            if (b.name.toLowerCase() === brandNameLower) {
              brand = b;
              break;
            }
          }
        }
        if (brand) {
          brandMap.set(brandCsv.id, brand);
        }

        // 국가 코드 추출 (2자리)
        const countryCode =
          brandCsv.country && brandCsv.country.length >= 2
            ? brandCsv.country.substring(0, 2).toUpperCase()
            : undefined;

        // 불리언 값 변환
        const isLuxury =
          brandCsv.is_luxury === "1" || brandCsv.is_luxury === "true";
        const isNiche =
          brandCsv.is_niche === "1" || brandCsv.is_niche === "true";
        const isPopular =
          brandCsv.is_popular === "1" || brandCsv.is_popular === "true";

        // 연도 변환
        const foundedYear = brandCsv.founded_year
          ? parseInt(brandCsv.founded_year)
          : undefined;

        if (!brand) {
          brand = new Brand(
            brandName,
            countryCode,
            brandCsv.country,
            brandCsv.description,
            foundedYear,
            brandCsv.website,
            brandCsv.designer,
            isLuxury,
            isNiche,
            isPopular,
            brandCsv.url
          );
          // this.em.persist(brand);
          logger.debug(`Created new brand: ${brand.name}`);
        } else {
          // 기존 브랜드 업데이트
          brand.countryCode = countryCode;
          brand.countryName = brandCsv.country;
          brand.description = brandCsv.description;
          brand.foundedYear = foundedYear;
          brand.website = brandCsv.website;
          brand.designer = brandCsv.designer;
          brand.isLuxury = isLuxury;
          brand.isNiche = isNiche;
          brand.isPopular = isPopular;
          brand.url = brandCsv.url;

          // this.em.persist(brand);
          logger.debug(`Updated existing brand: ${brand.name}`);
        }

        // CSV ID와 브랜드 객체를 맵에 저장
      }

      await this.em.flush();
      logger.info(`Successfully imported ${brandMap.size} brands`);
      return brandMap;
    } catch (error) {
      logger.error(`Error importing brands: ${error}`);
      throw error;
    }
  }

  async importPerfumes(
    filePath: string,
    brandMap: Map<string, Brand>
  ): Promise<Map<string, Perfume>> {
    const perfumeMap = new Map<string, Perfume>();

    try {
      const perfumes = await this.csvReader.readCsv<PerfumeCsv>(filePath);
      logger.info(`Importing ${perfumes.length} perfumes...`);

      for (const perfumeCsv of perfumes) {
        const brand = brandMap.get(perfumeCsv.brand_id);
        if (!brand) {
          logger.warn(
            `Brand not found for brand_id: ${perfumeCsv.brand_id}, perfume: ${perfumeCsv.title}, skipping...`
          );
          continue;
        }

        // 향수 농도 파싱
        let concentration: Concentration | undefined;
        if (perfumeCsv.gender) {
          concentration = this.parseConcentration(perfumeCsv.gender);
        }

        // 성별 파싱 - 숫자로 변환
        let gender: Gender | undefined;
        if (perfumeCsv.gender) {
          const genderEnum = this.parseGender(perfumeCsv.gender);
          if (genderEnum === Gender.MALE) gender = Gender.MALE;
          else if (genderEnum === Gender.FEMALE) gender = Gender.FEMALE;
          else if (genderEnum === Gender.UNISEX) gender = Gender.UNISEX;
        }

        // URL로 기존 향수 검색
        let perfume = await this.em.findOne(Perfume, { url: perfumeCsv.url });

        if (!perfume) {
          // 이름과 브랜드로 기존 향수 검색
          perfume = await this.em.findOne(Perfume, {
            name: perfumeCsv.title,
            brand: brand,
          });
        }

        const releaseYear = perfumeCsv.year
          ? parseInt(perfumeCsv.year)
          : undefined;

        if (!perfume) {
          perfume = new Perfume(
            perfumeCsv.title,
            brand,
            perfumeCsv.description,
            perfumeCsv.url,
            releaseYear,
            gender,
            concentration
          );
          this.em.persist(perfume);
          logger.debug(
            `Created new perfume: ${perfume.name} (Brand: ${brand.name})`
          );
        } else {
          // 기존 향수 업데이트
          perfume.name = perfumeCsv.title;
          perfume.brand = brand;
          perfume.description = perfumeCsv.description;
          perfume.url = perfumeCsv.url;
          perfume.releaseYear = releaseYear;
          perfume.gender = gender;
          perfume.concentration = concentration;
          this.em.persist(perfume);
          logger.debug(
            `Updated existing perfume: ${perfume.name} (Brand: ${brand.name})`
          );
        }

        // 디자이너 정보 처리
        await this.processDesigners(
          perfume,
          perfumeCsv.perfumer1,
          perfumeCsv.perfumer2
        );

        // CSV ID와 향수 객체를 맵에 저장
        perfumeMap.set(perfumeCsv.id, perfume);
      }

      await this.em.flush();
      logger.info(`Successfully imported ${perfumeMap.size} perfumes`);
      return perfumeMap;
    } catch (error) {
      logger.error(`Error importing perfumes: ${error}`);
      throw error;
    }
  }

  private async processDesigners(
    perfume: Perfume,
    perfumer1?: string,
    perfumer2?: string
  ): Promise<void> {
    if (
      perfumer1 &&
      perfumer1.trim() !== "unknown" &&
      perfumer1.trim() !== ""
    ) {
      await this.addDesignerToPerfume(
        perfume,
        perfumer1.trim(),
        DesignerRole.PERFUMER
      );
    }

    if (
      perfumer2 &&
      perfumer2.trim() !== "unknown" &&
      perfumer2.trim() !== "" &&
      perfumer2 !== perfumer1
    ) {
      await this.addDesignerToPerfume(
        perfume,
        perfumer2.trim(),
        DesignerRole.PERFUMER
      );
    }
  }

  private async addDesignerToPerfume(
    perfume: Perfume,
    designerName: string,
    role: DesignerRole
  ): Promise<void> {
    // 이미 연결된 디자이너인지 확인
    const existingRelation = await this.em.findOne(PerfumeDesigner, {
      perfume: perfume,
      designer: { name: designerName },
    });

    if (existingRelation) {
      logger.debug(
        `Designer ${designerName} is already linked to perfume ${perfume.name}`
      );
      return;
    }

    // 디자이너 찾기 또는 생성
    let designer = await this.em.findOne(Designer, { name: designerName });

    if (!designer) {
      designer = new Designer(designerName);
      this.em.persist(designer);
      logger.debug(`Created new designer: ${designer.name}`);
    }

    // 관계 생성
    const perfumeDesigner = new PerfumeDesigner(perfume, designer, role);
    this.em.persist(perfumeDesigner);
    logger.debug(`Linked designer ${designer.name} to perfume ${perfume.name}`);
  }

  async importNotes(
    filePath: string,
    perfumeMap: Map<string, Perfume>
  ): Promise<void> {
    try {
      const notes = await this.csvReader.readCsv<NoteCsv>(filePath);
      logger.info(`Importing ${notes.length} notes...`);

      const notesProcessed = new Set<string>();

      for (const noteCsv of notes) {
        if (!noteCsv.note_name || noteCsv.note_name.trim() === "") {
          logger.warn("Empty note name, skipping...");
          continue;
        }

        const perfumeId = parseInt(noteCsv.perfume_id);
        if (isNaN(perfumeId)) {
          logger.warn(`Invalid perfume ID: ${noteCsv.perfume_id}, skipping...`);
          continue;
        }

        const perfume = perfumeMap.get(perfumeId.toString());
        if (!perfume) {
          logger.warn(
            `Perfume with ID ${perfumeId} not found, skipping note ${noteCsv.note_name}...`
          );
          continue;
        }

        // 노트 타입 파싱
        const noteType = this.parseNoteType(noteCsv.note_type);

        // 노트 찾기 또는 생성
        let note = await this.em.findOne(Note, { name: noteCsv.note_name });

        if (!note) {
          note = new Note(noteCsv.note_name);
          this.em.persist(note);
          logger.debug(`Created new note: ${note.name}`);
        }

        // 이미 연결된 노트인지 확인
        const noteKey = `${perfumeId}-${note.name}-${noteType}`;
        if (notesProcessed.has(noteKey)) {
          continue;
        }

        const existingRelation = await this.em.findOne(PerfumeNote, {
          perfume: perfume,
          note: note,
          type: noteType,
        });

        if (!existingRelation) {
          const perfumeNote = new PerfumeNote(perfume, note, noteType);
          this.em.persist(perfumeNote);
          logger.debug(
            `Added note ${note.name} to perfume ${perfume.name} as ${noteType}`
          );
        }

        notesProcessed.add(noteKey);
      }

      await this.em.flush();
      logger.info(`Successfully imported ${notesProcessed.size} notes`);
    } catch (error) {
      logger.error(`Error importing notes: ${error}`);
      throw error;
    }
  }

  async importAccords(
    filePath: string,
    perfumeMap: Map<string, Perfume>
  ): Promise<void> {
    try {
      const accords = await this.csvReader.readCsv<AccordCsv>(filePath);
      logger.info(`Importing ${accords.length} accords...`);

      const accordsProcessed = new Set<string>();

      for (const accordCsv of accords) {
        if (!accordCsv.accord_name || accordCsv.accord_name.trim() === "") {
          logger.warn("Empty accord name, skipping...");
          continue;
        }

        const perfumeId = parseInt(accordCsv.perfume_id);
        if (isNaN(perfumeId)) {
          logger.warn(
            `Invalid perfume ID: ${accordCsv.perfume_id}, skipping...`
          );
          continue;
        }

        const perfume = perfumeMap.get(perfumeId.toString());
        if (!perfume) {
          logger.warn(
            `Perfume with ID ${perfumeId} not found, skipping accord ${accordCsv.accord_name}...`
          );
          continue;
        }

        // 어코드 찾기 또는 생성
        let accord = await this.em.findOne(Accord, {
          name: accordCsv.accord_name,
        });

        if (!accord) {
          accord = new Accord(accordCsv.accord_name);
          this.em.persist(accord);
          logger.debug(`Created new accord: ${accord.name}`);
        }

        // 이미 연결된 어코드인지 확인
        const accordKey = `${perfumeId}-${accord.name}`;
        if (accordsProcessed.has(accordKey)) {
          continue;
        }

        const existingRelation = await this.em.findOne(PerfumeAccord, {
          perfume: perfume,
          accord: accord,
        });

        if (!existingRelation) {
          const perfumeAccord = new PerfumeAccord(perfume, accord);
          this.em.persist(perfumeAccord);
          logger.debug(
            `Added accord ${accord.name} to perfume ${perfume.name}`
          );
        }

        accordsProcessed.add(accordKey);
      }

      await this.em.flush();
      logger.info(`Successfully imported ${accordsProcessed.size} accords`);
    } catch (error) {
      logger.error(`Error importing accords: ${error}`);
      throw error;
    }
  }

  private parseGender(gender: string): Gender | undefined {
    const normalized = gender.trim().toLowerCase();
    if (normalized.includes("men") && !normalized.includes("women")) {
      return Gender.MALE;
    } else if (normalized.includes("women")) {
      return Gender.FEMALE;
    } else if (normalized.includes("unisex")) {
      return Gender.UNISEX;
    }
    return undefined;
  }

  private parseConcentration(text: string): Concentration | undefined {
    const normalized = text.trim().toLowerCase();
    if (
      normalized.includes("parfum") ||
      normalized.includes("extrait") ||
      normalized.includes("extract")
    ) {
      return Concentration.PARFUM;
    } else if (
      normalized.includes("eau de parfum") ||
      normalized.includes("edp")
    ) {
      return Concentration.EAU_DE_PARFUM;
    } else if (
      normalized.includes("eau de toilette") ||
      normalized.includes("edt")
    ) {
      return Concentration.EAU_DE_TOILETTE;
    } else if (
      normalized.includes("eau de cologne") ||
      normalized.includes("edc")
    ) {
      return Concentration.EAU_DE_COLOGNE;
    } else if (
      normalized.includes("body spray") ||
      normalized.includes("mist")
    ) {
      return Concentration.BODY_SPRAY;
    }
    return undefined;
  }

  private parseNoteType(noteType: string): NoteType {
    const normalized = noteType.trim().toUpperCase();
    if (normalized === "TOP") {
      return NoteType.TOP;
    } else if (normalized === "MIDDLE" || normalized === "HEART") {
      return NoteType.MIDDLE;
    } else if (normalized === "BASE") {
      return NoteType.BASE;
    }
    return NoteType.TOP; // 기본값
  }
}
