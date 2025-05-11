import { Entity, Property, OneToMany, Collection } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { PerfumeAccord } from "./PerfumeAccord";

@Entity()
export class Accord extends BaseEntity {
  @Property({ unique: true })
  name!: string;

  @Property({ nullable: true })
  description?: string;

  @Property({ nullable: true })
  color?: string;

  @OneToMany(() => PerfumeAccord, (perfumeAccord) => perfumeAccord.accord)
  perfumeAccords = new Collection<PerfumeAccord>(this);

  constructor(name: string, description?: string, color?: string) {
    super();
    this.name = name;
    this.description = description;
    this.color = color;
  }
}
