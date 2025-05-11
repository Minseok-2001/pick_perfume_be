import {
  Entity,
  Property,
  ManyToOne,
  OneToMany,
  Collection,
  Enum,
  type EnumType,
} from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { Brand } from "./Brand";
import { PerfumeNote } from "./PerfumeNote";
import { PerfumeAccord } from "./PerfumeAccord";
import { PerfumeDesigner } from "./PerfumeDesigner";

export enum Gender {
  MALE = "MALE",
  FEMALE = "FEMALE",
  UNISEX = "UNISEX",
}

export enum Concentration {
  PARFUM = "parfum",
  EAU_DE_PARFUM = "eau_de_parfum",
  EAU_DE_TOILETTE = "eau_de_toilette",
  EAU_DE_COLOGNE = "eau_de_cologne",
  BODY_SPRAY = "body_spray",
}

@Entity()
export class Perfume extends BaseEntity {
  @Property()
  name!: string;

  @ManyToOne(() => Brand)
  brand!: Brand;

  @Property({ nullable: true })
  description?: string;

  @Property({ nullable: true })
  url?: string;

  @Property({ nullable: true })
  releaseYear?: number;

  @Property({ nullable: true })
  isApproved?: boolean;

  @Enum({ items: () => Gender, nullable: true, type: "string" })
  gender?: Gender;

  @Enum({ items: () => Concentration, nullable: true, type: "string" })
  concentration?: Concentration;

  @OneToMany(() => PerfumeNote, (note) => note.perfume)
  notes = new Collection<PerfumeNote>(this);

  @OneToMany(() => PerfumeAccord, (accord) => accord.perfume)
  accords = new Collection<PerfumeAccord>(this);

  @OneToMany(() => PerfumeDesigner, (designer) => designer.perfume)
  designers = new Collection<PerfumeDesigner>(this);

  constructor(
    name: string,
    brand: Brand,
    description?: string,
    url?: string,
    releaseYear?: number,
    gender?: Gender,
    concentration?: Concentration,
    isApproved?: boolean
  ) {
    super();
    this.name = name;
    this.brand = brand;
    this.description = description;
    this.url = url;
    this.releaseYear = releaseYear;
    this.gender = gender;
    this.concentration = concentration;
    this.isApproved = isApproved;
  }
}
