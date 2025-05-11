import { Entity, ManyToOne, Enum, type EnumType } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { Perfume } from "./Perfume";
import { Designer } from "./Designer";

export enum DesignerRole {
  PERFUMER = "perfumer",
  CREATIVE_DIRECTOR = "creative_director",
  NOSE = "nose",
}

@Entity()
export class PerfumeDesigner extends BaseEntity {
  @ManyToOne(() => Perfume)
  perfume!: Perfume;

  @ManyToOne(() => Designer)
  designer!: Designer;

  @Enum({ items: () => DesignerRole, type: "string" })
  role: DesignerRole = DesignerRole.PERFUMER;

  constructor(
    perfume: Perfume,
    designer: Designer,
    role: DesignerRole = DesignerRole.PERFUMER
  ) {
    super();
    this.perfume = perfume;
    this.designer = designer;
    this.role = role;
  }
}
