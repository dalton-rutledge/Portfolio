
class sphere{
    constructor(center, radius, material){
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    hit(r, rec, t_min, t_max){
        var oc = subtract(r.origin(), this.center);
        var a = dot(r.direction(), r.direction());
        var b = dot(oc, r.direction());
        var c = dot(oc, oc) - this.radius * this.radius; 
        var discriminant = b*b - a*c;
        if (discriminant > 0){
            var temp = (-1*b - Math.sqrt(discriminant)) / (a)
            if (temp < t_max && temp > t_min){
                rec.t = temp;
                rec.p = r.point_at_parameter(rec.t);
                rec.normal = normalize(subtract(rec.p, this.center)); 
                if (this.material){ rec.material = this.material;}
                return true; 
            }
            var temp = (-1*b + Math.sqrt(discriminant)) / (a);
            if (temp < t_max && temp > t_min){
                rec.t = temp;
                rec.p = r.point_at_parameter(rec.t); 
                rec.normal = normalize(subtract(rec.p, this.center)); 
                if (this.material){ rec.material = this.material;}
                return true;
            }
        }
        return false; 
    }
}