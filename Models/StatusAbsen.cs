using System;
using System.Collections.Generic;

namespace LKS_ITSSA_2025.Models;

public partial class StatusAbsen
{
    public int Id { get; set; }

    public string? Status { get; set; }

    public virtual ICollection<AbsenUser> AbsenUsers { get; set; } = new List<AbsenUser>();
}
