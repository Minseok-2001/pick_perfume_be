import { Options } from "@mikro-orm/core";
import { TsMorphMetadataProvider } from "@mikro-orm/reflection";
import { MySqlDriver } from "@mikro-orm/mysql";
import * as dotenv from "dotenv";
import { Brand } from "../../src/entities/Brand";
import { Perfume } from "../../src/entities/Perfume";
import { Note } from "../../src/entities/Note";
import { Accord } from "../../src/entities/Accord";
import { PerfumeNote } from "../../src/entities/PerfumeNote";
import { PerfumeAccord } from "../../src/entities/PerfumeAccord";
import { Designer } from "../../src/entities/Designer";
import { PerfumeDesigner } from "../../src/entities/PerfumeDesigner";

dotenv.config();

const config: Options = {
  metadataProvider: TsMorphMetadataProvider,
  entities: [
    Brand,
    Perfume,
    Note,
    Accord,
    PerfumeNote,
    PerfumeAccord,
    Designer,
    PerfumeDesigner,
  ],
  type: "mysql",
  driver: MySqlDriver,
  dbName: process.env.DB_NAME || "pick_perfume",
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT) || 3396,
  user: process.env.DB_USER || "dev",
  password: process.env.DB_PASSWORD || "1234",
  debug: true,
  logger: console.log.bind(console),
  allowGlobalContext: true,
  migrations: {
    path: "src/migrations",
    disableForeignKeys: false,
  },
};

export default config;
