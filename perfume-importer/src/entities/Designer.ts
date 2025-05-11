import { Entity, Property, OneToMany, Collection } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { PerfumeDesigner } from "./PerfumeDesigner";

@Entity()
export class Designer extends BaseEntity {
  @Property({ unique: true })
  name!: string;

  @Property({ nullable: true })
  biography?: string;

  @Property({ nullable: true })
  country?: string;

  @OneToMany(
    () => PerfumeDesigner,
    (perfumeDesigner) => perfumeDesigner.designer
  )
  perfumeDesigners = new Collection<PerfumeDesigner>(this);

  constructor(name: string, biography?: string, country?: string) {
    super();
    this.name = name;
    this.biography = biography;
    this.country = country;
  }
}
