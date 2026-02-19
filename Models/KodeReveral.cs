using System;
using System.Collections.Generic;

namespace LKS_ITSSA_2025.Models;

public partial class KodeReveral
{
    public int Id { get; set; }

    public string? Code { get; set; }

    public int? UserId { get; set; }

    public virtual User? User { get; set; }

    public virtual ICollection<User> Users { get; set; } = new List<User>();
}
